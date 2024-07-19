package me.redstoner2019.client.gui;

import me.redstoner2019.*;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.github.GitHubReleasesFetcher;
import me.redstoner2019.server.CacheServer;
import org.json.JSONObject;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main extends JFrame {

    private DownloadStatus status = new DownloadStatus();

    public Main() throws Exception {
        setSize(1280,720);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        /**
         * Base Layout
         */

        ODLayout baseLayout = new ODLayout();

        baseLayout.addColumn(new Column(10, LengthType.PIXEL));
        baseLayout.addColumn(new Column(Lengths.VARIABLE));
        baseLayout.addColumn(new Column(10,LengthType.PIXEL));

        baseLayout.addRow(new Row(Lengths.VARIABLE));
        baseLayout.addRow(new Row(10,LengthType.PIXEL));
        baseLayout.addRow(new Row(50,LengthType.PIXEL));
        baseLayout.addRow(new Row(10,LengthType.PIXEL));

        JProgressBar downloadProgress = new JProgressBar();
        JPanel mainPanel = new JPanel();

        add(downloadProgress);
        add(mainPanel);

        baseLayout.registerComponent(downloadProgress,new Position(1,2));
        baseLayout.registerComponent(mainPanel,new Position(1,0));

        setLayout(baseLayout);

        downloadProgress.setStringPainted(true);

        /**
         * Main Pane layout
         */

        ODLayout mainLayout = new ODLayout();

        mainLayout.addColumn(new Column(300,LengthType.PIXEL));
        mainLayout.addColumn(new Column(Lengths.VARIABLE));
        mainLayout.addColumn(new Column(300,LengthType.PIXEL));

        mainLayout.addRow(new Row(Lengths.VARIABLE));
        mainLayout.addRow(new Row(10,LengthType.PIXEL));

        JPanel leftPanel = new JPanel();
        JPanel middlePanel = new JPanel();
        JPanel rightPanel = new JPanel();

        mainPanel.add(leftPanel);
        mainPanel.add(middlePanel);
        mainPanel.add(rightPanel);

        mainLayout.registerComponent(leftPanel,new Position(0,0));
        mainLayout.registerComponent(middlePanel,new Position(1,0));
        mainLayout.registerComponent(rightPanel,new Position(2,0));

        mainPanel.setLayout(mainLayout);

        /**
         * Left Panel
         */

        ODLayout leftLayout = new ODLayout();
        leftLayout.addRow(new Row(Lengths.VARIABLE));
        leftLayout.addRow(new Row(Lengths.VARIABLE));

        leftLayout.addColumn(new Column(Lengths.VARIABLE));

        JList<String> versions = new JList<>();
        JScrollPane versionsScroll = new JScrollPane(versions);
        leftPanel.add(versionsScroll);

        DefaultListModel<String> filesModel = new DefaultListModel<>();
        JList<String> files = new JList<>(filesModel);
        JScrollPane filesScroll = new JScrollPane(files);
        leftPanel.add(filesScroll);

        leftLayout.registerComponent(versionsScroll,new Position(0,0));
        leftLayout.registerComponent(filesScroll,new Position(0,1));

        leftPanel.setLayout(leftLayout);

        /**
         * Middle Panel
         */

        ODLayout middleLayout = new ODLayout();

        middleLayout.addColumn(new Column(0.2,LengthType.PERCENT));
        middleLayout.addColumn(new Column(Lengths.VARIABLE));
        middleLayout.addColumn(new Column(0.2,LengthType.PERCENT));

        middleLayout.addRow(new Row(Lengths.VARIABLE));
        middleLayout.addRow(new Row(40,LengthType.PIXEL));

        JButton launch = new JButton("Launch");

        middlePanel.add(launch);

        middleLayout.registerComponent(launch,new Position(1,1));

        middlePanel.setLayout(middleLayout);

        /**
         * Right Panel
         */

        ListModel<String> gamesListModel = new DefaultListModel<>();
        JList<String> games = new JList<>(gamesListModel);
        JScrollPane gamesScroll = new JScrollPane(games);
        rightPanel.add(gamesScroll);
        rightPanel.setLayout(new GridLayout());
        games.setListData(new String[]{"ODGraphics", "FNaF"});


        games.addListSelectionListener(e -> {
            if(games.getSelectedValue() != null) {
                Thread t = new Thread(() -> {
                    List<String> versionsList = null;
                    try {
                        versionsList = GitHubReleasesFetcher.fetchAllReleases("Redstoner-2019", games.getSelectedValue());
                        versions.setListData(versionsList.toArray(String[]::new));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
                t.start();
            }
        });

        List<String> filesList = new ArrayList<>();
        versions.addListSelectionListener(e -> {
            if(versions.getSelectedValue() != null && games.getSelectedValue() != null) {
                filesList.clear();
                try {
                    filesList.addAll(GitHubReleasesFetcher.fetchAllReleaseFiles("Redstoner-2019", games.getSelectedValue(),versions.getSelectedValue()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                filesModel.removeAllElements();
                String[] names = new String[filesList.size()];
                for (int i = 0; i < names.length; i++) {
                    String[] split = filesList.get(i).split("/");
                    filesModel.add(i,split[split.length-1]);
                }
            }
        });

        Main main = this;



        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(versions.getSelectedValue() != null && games.getSelectedValue() != null) {
                        String destination ="files/Redstoner-2019/" + games.getSelectedValue() + "/" + versions.getSelectedValue() + "/download.jar";

                        if(!new File(destination).exists()) {
                            FileDownloader.downloadFile(filesList.get(files.getSelectedIndex()), destination, status);
                        } else {
                            status.setComplete(true);
                        }

                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (!status.isComplete()){
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                                try {
                                    Process p = Runtime.getRuntime().exec("java -jar " + destination);
                                } catch (IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        });
                        t.start();
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                /*if(versions.getSelectedValue() != null && games.getSelectedValue() != null) {
                    String url = "";
                    try {
                        for(String s : files){
                            System.out.println(s);
                            if(s.contains(Utilities.getPlatform())){
                                url = s;
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    downloadFile(url, "download.jar", status);

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!status.isComplete()){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            System.out.println("Starting");
                            try {
                                Process p = Runtime.getRuntime().exec("java -jar download.jar");
                                Scanner scanner = new Scanner(p.getInputStream());
                                while (p.isAlive()) {
                                    System.out.println(scanner.nextLine());
                                }
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    t.start();
                }
                else System.out.println("Null");*/
            }
        });

        /**
         * Setup complete
         */

        setVisible(true);

        Thread console = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (true) {
                    System.out.println("Command?");
                    String command = scanner.nextLine();
                    JSONObject request = new JSONObject();
                    switch (command) {
                        case "get" -> {
                            request.put("header","request-data");
                        }
                        case "add-user" -> {
                            request.put("header","add-user");
                            System.out.println("User?");
                            String user = scanner.nextLine();
                            request.put("user",user);
                        }
                        case "add-repo" -> {
                            request.put("header","add-repo");
                            System.out.println("User?");
                            String user = scanner.nextLine();
                            request.put("user",user);
                            System.out.println("Repo?");
                            String repo = scanner.nextLine();
                            request.put("repo", repo);
                        }
                        case "add-version" -> {
                            request.put("header","add-version");
                            System.out.println("User?");
                            String user = scanner.nextLine();
                            request.put("user",user);
                            System.out.println("Repo?");
                            String repo = scanner.nextLine();
                            request.put("repo", repo);
                            System.out.println("Version?");
                            String version = scanner.nextLine();
                            request.put("version", version);
                        }
                    }
                    try {
                        Socket socket = new Socket("localhost",8001);
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        oos.writeObject(request.toString());
                        JSONObject result = new JSONObject((String) ois.readObject());
                        System.out.println(CacheServer.prettyJSON(result.toString()));
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        console.start();

        while (true){
            downloadProgress.setValue((int) status.getBytesRead());
            downloadProgress.setMaximum((int) status.getBytesTotal());

            downloadProgress.setString(String.format("%d / %d (%.2f%%) - Status: %d, Complete: %s",downloadProgress.getValue(),downloadProgress.getMaximum(),((float) downloadProgress.getValue() / (float) downloadProgress.getMaximum()) * 100, status.getStatus(), status.isComplete() + ""));
        }
    }
    public static void main(String[] args) throws Exception {
        new Main();
    }
}