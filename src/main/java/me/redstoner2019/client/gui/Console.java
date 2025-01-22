package me.redstoner2019.client.gui;

import me.redstoner2019.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Console extends JFrame {

    private DefaultListModel<Log> model = new DefaultListModel<Log>();
    private JList<Log> list = new JList<>(model);
    private JScrollPane pane;
    private boolean isInitialized = false;
    private Process process;

    public Console(Process process) {
        this.process = process;
    }

    public void run(){
        setSize(720,720);
        setResizable(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("OD Console");

        pane = new JScrollPane(list);

        list.setSelectionBackground(list.getBackground());

        list.setCellRenderer(new LogRenderer());

        this.add(pane);

        setVisible(true);

        ODLayout layout = new ODLayout();

        layout.addColumn(new Column(Lengths.VARIABLE));
        layout.addColumn(new Column(70,LengthType.PIXEL));
        layout.addRow(new Row(Lengths.VARIABLE));
        layout.addRow(new Row(30,LengthType.PIXEL));

        JTextField commandField = new JTextField();
        this.add(commandField);

        JButton runButton = new JButton("Run");
        this.add(runButton);

        layout.registerComponent(pane,new Position(0,0), new Position(1,0));
        layout.registerComponent(commandField,new Position(0,1));
        layout.registerComponent(runButton,new Position(1,1));

        setLayout(layout);

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(process.isAlive()){
                    try {
                        process.getOutputStream().write(commandField.getText().getBytes());
                        process.getOutputStream().flush();
                        commandField.setText("");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });

        isInitialized = true;
    }

    public void log(Level level, String message){
        JScrollBar sb = pane.getVerticalScrollBar();
        boolean scrollDown = sb.getValue() == sb.getMinimum();

        model.addElement(new Log(level, message));

        if(scrollDown){
            list.ensureIndexIsVisible(model.size() - 1);
        }
    }

    public void close() {
        setVisible(false);
        dispose();
        String defaults = " font-family: consolas; font-size: 10px; white-space: break-spaces";
    }

    static class LogRenderer extends JPanel implements ListCellRenderer<Log> {
        private final JLabel time;
        private final JLabel type;
        private final JLabel message;

        public LogRenderer() {
            setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

            time = new JLabel();
            time.setFont(new Font("Consolas", Font.BOLD, 14));

            type = new JLabel();
            type.setFont(new Font("Consolas", Font.BOLD, 14));

            message = new JLabel();
            message.setFont(new Font("Consolas", Font.PLAIN, 14));

            add(type);
            add(time);
            add(message);

            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Log> list,
                Log log,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            if (log == null) {
                time.setText("No Data");
                message.setText("");
            } else {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(log.getTimestamp()), ZoneId.systemDefault())
                        .format(formatter);

                time.setText(formattedDate);
                type.setText(String.format("%-5s",log.getLevel().name()));
                message.setText(": " + log.getMessage());

                switch (log.getLevel()){
                    case INFO -> type.setForeground(Color.GREEN);
                    case ERR -> type.setForeground(Color.RED);
                    case WARN -> type.setForeground(Color.ORANGE);
                }
            }
            return this;
        }
    }

}

enum Level{
    INFO,
    ERR,
    WARN
}

class Log{
    private Level level;
    private String message;
    private long timestamp;

    public Log(Level level, String message){
        this.level = level;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }

    public Level getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Log{" +
                "level=" + level +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}