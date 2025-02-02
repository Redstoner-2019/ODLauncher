import com.formdev.flatlaf.IntelliJTheme;
import me.redstoner2019.*;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.gui.Main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Test {
    public static void main(String[] args) {
        IntelliJTheme.setup(Main.class.getResourceAsStream("/themes/theme.purple.json"));

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);

        ODLayout layout = new ODLayout();
        frame.setLayout(layout);

        layout.addColumn(new Column(10, LengthType.PIXEL));
        layout.addColumn(new Column(Lengths.VARIABLE));
        layout.addColumn(new Column(10, LengthType.PIXEL));
        layout.addColumn(new Column(200, LengthType.PIXEL));
        layout.addColumn(new Column(10, LengthType.PIXEL));
        layout.addColumn(new Column(Lengths.VARIABLE));
        layout.addColumn(new Column(10, LengthType.PIXEL));

        layout.addRow(new Row(Lengths.VARIABLE));
        layout.addRow(new Row(30, LengthType.PIXEL));
        layout.addRow(new Row(10, LengthType.PIXEL));
        layout.addRow(new Row(30, LengthType.PIXEL));
        layout.addRow(new Row(10, LengthType.PIXEL));
        layout.addRow(new Row(30, LengthType.PIXEL));
        layout.addRow(new Row(10, LengthType.PIXEL));
        layout.addRow(new Row(30, LengthType.PIXEL));
        layout.addRow(new Row(10, LengthType.PIXEL));

        frame.setLayout(layout);

        JProgressBar generalProgress = new JProgressBar();
        JProgressBar actionProgress = new JProgressBar();
        JButton downloadButton = new JButton("Download");
        JLabel downloadLabel = new JLabel("");

        frame.add(generalProgress);
        frame.add(actionProgress);
        frame.add(downloadButton);
        frame.add(downloadLabel);

        layout.registerComponent(generalProgress,new Position(1,3),new Position(5,3));
        layout.registerComponent(actionProgress,new Position(1,5),new Position(5,5));
        layout.registerComponent(downloadLabel,new Position(1,7),new Position(5,7));
        layout.registerComponent(downloadButton,new Position(3,1));

        frame.setVisible(true);

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DownloadStatus generalStat = new DownloadStatus();
                DownloadStatus actionStat = new DownloadStatus();

                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FileDownloader.downloadRelease("https://github.com/Redstoner-2019/FNaF/releases/tag/v1.4.1-alpha.1".replaceAll("tag","download"),"gameTest/Redstoner-2019/FNaF",actionStat, generalStat);
                        while (!generalStat.isComplete()) {
                            actionProgress.setMaximum((int) actionStat.getBytesTotal());
                            actionProgress.setValue((int) actionStat.getBytesRead());

                            generalProgress.setMaximum((int) generalStat.getBytesTotal() * 1000);
                            generalProgress.setValue((int) (generalStat.getBytesRead() * 1000) + (int) (actionStat.getPercent()*10));

                            generalProgress.setString(String.format("%d / %d %.2f%%",generalStat.getBytesRead(),generalStat.getBytesTotal(), ((double) generalProgress.getValue() / generalProgress.getMaximum()) * 100));
                            generalProgress.setStringPainted(true);

                            actionProgress.setString(String.format(" %.2f%%", actionStat.getPercent()));
                            actionProgress.setStringPainted(true);

                            downloadLabel.setText(actionStat.getMessage());

                            frame.revalidate();
                            frame.repaint();

                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        generalProgress.setMaximum((int) generalStat.getBytesTotal());
                        generalProgress.setValue((int) generalStat.getBytesRead());

                        actionProgress.setMaximum((int) actionStat.getBytesTotal());
                        actionProgress.setValue((int) actionStat.getBytesRead());

                        generalProgress.setString("Done!");
                        actionProgress.setString("Done!");

                        System.out.println("Done!");
                    }
                });
                t.start();
            }
        });

        DownloadStatus status = new DownloadStatus();
    }
}