package me.redstoner2019.client.gui;

import me.redstoner2019.*;
import me.redstoner2019.ODLayout;
import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.github.GitHubReleasesFetcher;
import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
    private JSONObject onlineData = null;
    private JSONObject cacheData = null;
    private boolean offlineMode = false;
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient();
    private StatisticClient statsClient = null;

    public Main() {
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
        leftLayout.addRow(new Row(20,LengthType.PIXEL));
        leftLayout.addRow(new Row(Lengths.VARIABLE));
        leftLayout.addRow(new Row(20,LengthType.PIXEL));
        leftLayout.addRow(new Row(Lengths.VARIABLE));

        leftLayout.addColumn(new Column(Lengths.VARIABLE));

        DefaultListModel<String> versionModel = new DefaultListModel<>();
        JList<String> versions = new JList<>(versionModel);
        JScrollPane versionsScroll = new JScrollPane(versions);
        leftPanel.add(versionsScroll);

        DefaultListModel<String> filesModel = new DefaultListModel<>();
        JList<String> files = new JList<>(filesModel);
        JScrollPane filesScroll = new JScrollPane(files);
        leftPanel.add(filesScroll);

        leftLayout.registerComponent(versionsScroll,new Position(0,1));
        leftLayout.registerComponent(filesScroll,new Position(0,3));

        leftPanel.setLayout(leftLayout);

        /**
         * Middle Panel
         */

        ODLayout middleLayout = new ODLayout();

        middleLayout.addColumn(new Column(0.2,LengthType.PERCENT));
        middleLayout.addColumn(new Column(Lengths.VARIABLE));
        middleLayout.addColumn(new Column(0.2,LengthType.PERCENT));

        middleLayout.addRow(new Row(40,LengthType.PIXEL));
        middleLayout.addRow(new Row(Lengths.VARIABLE));
        middleLayout.addRow(new Row(20,LengthType.PIXEL));
        middleLayout.addRow(new Row(40,LengthType.PIXEL));

        JTextArea infoLabel = new JTextArea();
        infoLabel.setEditable(false);
        infoLabel.setLineWrap(true);

        JButton launch = new JButton("Launch");

        middlePanel.add(launch);
        middlePanel.add(infoLabel);

        middleLayout.registerComponent(launch,new Position(1,3));
        middleLayout.registerComponent(infoLabel,new Position(1,1));

        middlePanel.setLayout(middleLayout);

        /**
         * Right Panel
         */

        ODLayout rightLayout = new ODLayout();
        rightLayout.addRow(new Row(20,LengthType.PIXEL));
        rightLayout.addRow(new Row(Lengths.VARIABLE));
        rightLayout.addRow(new Row(20,LengthType.PIXEL));
        rightLayout.addRow(new Row(Lengths.VARIABLE));

        rightLayout.addColumn(new Column(Lengths.VARIABLE));

        DefaultListModel<String> gamesListModel = new DefaultListModel<>();
        JList<String> games = new JList<>(gamesListModel);
        JScrollPane gamesScroll = new JScrollPane(games);
        rightPanel.add(gamesScroll);

        DefaultListModel<String> authorModel = new DefaultListModel<>();
        JList<String> authors = new JList<>(authorModel);
        JScrollPane authorScroll = new JScrollPane(authors);
        rightPanel.add(authorScroll);

        rightLayout.registerComponent(gamesScroll,new Position(0,3));
        rightLayout.registerComponent(authorScroll,new Position(0,1));

        rightPanel.setLayout(rightLayout);

        List<String> filesList = new ArrayList<>();

        authors.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JSONObject gamesObject = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue());
                gamesListModel.clear();
                gamesListModel.addAll(gamesObject.keySet());
            }
        });

        games.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JSONObject versionsObject = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue()).getJSONObject(games.getSelectedValue());
                versionModel.clear();
                versionModel.addAll(versionsObject.keySet());
            }
        });

        versions.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                JSONArray assets = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue()).getJSONObject(games.getSelectedValue()).getJSONArray(versions.getSelectedValue());
                filesModel.clear();
                for (int i = 0; i < assets.length(); i++) {
                    filesModel.add(i,assets.getJSONObject(i).getString("filename"));
                }
            }
        });

        files.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

            }
        });

        /*games.addListSelectionListener(e -> {
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

        files.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try {
                    infoLabel.setText(CacheServer.prettyJSON(FileDownloader.getFileInfo("Redstoner-2019",games.getSelectedValue(),versions.getSelectedValue(),files.getSelectedValue()).toString()));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });*/

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
            }
        });

        /**
         * Setup complete
         */

        setVisible(true);

        try{
            onlineData = Utilities.getIPData();
            System.out.println(onlineData);
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't retrieve ip data. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }

        try{
            cacheData = runCacheCommand(new JSONObject("{\"header\":\"request-data\"}"));
            JSONObject authorsObject = cacheData.getJSONObject("repos");
            authorModel.clear();
            authorModel.addAll(authorsObject.keySet());
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't retrieve cache server data. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }

        try{
            if(!offlineMode){
                authenticatorClient.setAddress(onlineData.getString("auth-server"));
                authenticatorClient.setPort(onlineData.getInt("auth-server-port"));

                authenticatorClient.setup();
            }
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't connect to auth Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }

        try{
            if(!offlineMode){
                statsClient = new StatisticClient(onlineData.getString("statistics-server"),onlineData.getInt("statistics-server-port"));
                statsClient.sendRequest(new JSONObject());
            }
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't connect to auth Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }

        Thread console = new Thread(new Runnable() {
            @Override
            public void run() {
                Scanner scanner = new Scanner(System.in);
                while (!offlineMode) {
                    System.out.println("Command?");
                    String command = scanner.nextLine();
                    JSONObject request = new JSONObject();
                    switch (command) {
                        case "get" -> {
                            request.put("header","request-data");
                        }
                        case "refresh" -> {
                            request.put("header","refresh");
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
                        System.out.println("request " + request);
                        String host = onlineData.getString("cache-server");
                        int port = onlineData.getInt("cache-server-port");
                        Socket socket = new Socket(host,port);
                        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                        oos.writeObject(request.toString());
                        JSONObject result = new JSONObject((String) ois.readObject());
                        System.out.println(CacheServer.prettyJSON(result.toString()));
                    } catch (IOException | ClassNotFoundException e) {
                        JOptionPane.showMessageDialog(main,"Couldn't connect to Cache Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
                        offlineMode = true;
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

    public JSONObject runCacheCommand(JSONObject request){
        if(offlineMode) return null;
        try {
            String host = onlineData.getString("cache-server");
            int port = onlineData.getInt("cache-server-port");
            Socket socket = new Socket(host,port);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            oos.writeObject(request.toString());
            JSONObject result = new JSONObject((String) ois.readObject());
            return result;
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this,"Couldn't connect to Cache Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
            offlineMode = true;
            return null;
        }
    }

    public static void printStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        // Start from index 1 to skip the getStackTrace() method itself
        for (int i = 1; i < stackTraceElements.length; i++) {
            System.out.println(stackTraceElements[i]);
        }
    }
}