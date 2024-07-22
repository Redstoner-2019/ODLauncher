package me.redstoner2019.client.gui;

import me.redstoner2019.*;
import me.redstoner2019.ODLayout;
import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.github.CacheRequest;
import me.redstoner2019.client.github.GitHub;
import me.redstoner2019.server.CacheServer;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Main extends JFrame {

    private DownloadStatus status = new DownloadStatus();
    private boolean offlineMode = false;
    private AuthenticatorClient authenticatorClient = new AuthenticatorClient();
    private StatisticClient statsClient = null;
    private Main main;
    private boolean isLoggedIn = false;
    private String TOKEN = "";
    private String username = "";
    private String displayname = "";

    private List<Profile> profiles = new ArrayList<>();

    public Main() throws IOException {
        main = this;

        File configSaveFile = new File("odlauncher/config.json");

        JSONObject config = new JSONObject(CacheServer.readFile(configSaveFile));

        if(config.has("token")) TOKEN = config.getString("token");

        try {
            authenticatorClient.setPort(Utilities.getIPData().getInt("auth-server-port"));
            authenticatorClient.setAddress(Utilities.getIPData().getString("auth-server"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
        }
        authenticatorClient.setup();

        setSize(1280,720);
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("OD Launcher");

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.setFont(new Font("Arial",Font.PLAIN,20));

        /**
         * Launcher
         */

        DefaultListModel<Profile> profilesModel = new DefaultListModel<>();
        JList<Profile> profiles = new JList<>(profilesModel);
        JList<Profile> profileJList = new JList<>(profilesModel);
        profiles.setCellRenderer(new ProfileRenderer());
        JScrollPane scrollPane = new JScrollPane(profiles);
        JLabel info = new JLabel();
        JScrollPane versionInfo = new JScrollPane(info);
        JProgressBar progress = new JProgressBar();
        JButton launch = new JButton("Launch");

        if(config.has("profiles")){
            JSONObject o = config.getJSONObject("profiles");
            for(String uuid : o.keySet()){
                profilesModel.add(0,new Profile().fromJSON(o.getJSONObject(uuid)));
            }
        }

        /*try {
            profilesModel.add(0,new Profile(ImageIO.read(new File("C:\\Users\\Redstoner_2019\\Pictures\\Screenshot 2024-06-16 183803.png")),"This is a really really really really really really really really really really long Game Name","v1.3.0-alpha.1","Profile 1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            profilesModel.add(0,new Profile(ImageIO.read(new File("C:\\Users\\Redstoner_2019\\Pictures\\Screenshot 2024-06-16 183803.png")),"FNaF","v1.3.0-alpha.1", "Profile 2"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            profilesModel.add(0,new Profile(ImageIO.read(new File("C:\\Users\\Redstoner_2019\\Pictures\\Screenshot 2024-06-16 183803.png")),"FNaF","v1.3.0-alpha.1", "Profile 2"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        profiles.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println(profiles.getSelectedIndex());
            }
        });

        /**
         * Profiles
         */

        /**
         * Login
         */

        JLabel loggedInAs = new JLabel("Not logged in");

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        JTextField usernameCreation = new JTextField();
        JTextField displayNameCreation = new JTextField();
        JPasswordField passwordCreation = new JPasswordField();
        JPasswordField passwordCreationConfirm = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton logoutButton = new JButton("Logout");

        JButton createButton = new JButton("Create Account");
        JLabel actionInfo = new JLabel("");

        /**
         * Settings
         */


        /**
         * init setup
         */

        JSONObject result = authenticatorClient.tokeninfo(TOKEN);
        System.out.println(result);
        if(!result.getString("data").equals("token-not-found")){
            displayname = result.getString("displayname");
            username = result.getString("username");
            loggedInAs.setText("Logged in as: " + displayname);
            actionInfo.setText("Login success.");
            actionInfo.setForeground(Color.GREEN);
            isLoggedIn = true;
        }

        {
            JPanel panel = new JPanel();
            ODLayout layout = new ODLayout();
            layout.addRow(new Row(Lengths.VARIABLE));
            layout.addRow(new Row(100,LengthType.PIXEL));
            layout.addColumn(new Column(Lengths.VARIABLE));

            JPanel topPanel = new JPanel();

            ODLayout topLayout = new ODLayout();

            topLayout.addColumn(new Column(10,LengthType.PIXEL));
            topLayout.addColumn(new Column(200,LengthType.PIXEL));
            topLayout.addColumn(new Column(10,LengthType.PIXEL));
            topLayout.addColumn(new Column(Lengths.VARIABLE));
            topLayout.addColumn(new Column(10,LengthType.PIXEL));
            topLayout.addRow(new Row(10,LengthType.PIXEL));
            topLayout.addRow(new Row(30,LengthType.PIXEL));
            topLayout.addRow(new Row(10,LengthType.PIXEL));
            topLayout.addRow(new Row(Lengths.VARIABLE));
            topLayout.addRow(new Row(10,LengthType.PIXEL));

            JLabel selectProfile = new JLabel("<html><u>Select Profile</u></html>");
            JLabel profileInfo = new JLabel("<html><u>Profile Info</u></html>");

            selectProfile.setFont(new Font("Arial",Font.PLAIN,20));
            profileInfo.setFont(new Font("Arial",Font.PLAIN,20));

            selectProfile.setHorizontalAlignment(JLabel.CENTER);
            profileInfo.setHorizontalAlignment(JLabel.CENTER);

            progress.setStringPainted(true);
            progress.setString("Downloading 0/0 MB (0.00%)");

            topPanel.add(scrollPane);
            topPanel.add(versionInfo);
            topPanel.add(selectProfile);
            topPanel.add(profileInfo);

            topLayout.registerComponent(scrollPane,new Position(1,3));
            topLayout.registerComponent(selectProfile,new Position(1,1));
            topLayout.registerComponent(profileInfo,new Position(3,1));
            topLayout.registerComponent(versionInfo,new Position(3,3));

            topPanel.setLayout(topLayout);
            panel.add(topPanel);

            JPanel bottomPanel = new JPanel();
            ODLayout bottomLayout = new ODLayout();

            bottomLayout.addColumn(new Column(10,LengthType.PIXEL));
            bottomLayout.addColumn(new Column(Lengths.VARIABLE));
            bottomLayout.addColumn(new Column(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(Lengths.VARIABLE));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(Lengths.VARIABLE));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));

            bottomPanel.add(launch);
            bottomPanel.add(progress);

            bottomLayout.registerComponent(launch,new Position(1,1));
            bottomLayout.registerComponent(progress,new Position(1,3));

            bottomPanel.setLayout(bottomLayout);
            panel.add(bottomPanel);

            layout.registerComponent(topPanel,new Position(0,0));
            layout.registerComponent(bottomPanel,new Position(0,1));
            panel.setLayout(layout);
            tabbedPane.addTab("Launcher", panel);
        }

        DefaultListModel<String> gamesModel = new DefaultListModel<>();
        DefaultListModel<String> versionsModel = new DefaultListModel<>();
        DefaultListModel<String> filesModel = new DefaultListModel<>();

        JList<String> games = new JList<>(gamesModel);
        JList<String> versions = new JList<>(versionsModel);
        JList<String> files = new JList<>(filesModel);

        {
            JPanel panel = new JPanel();

            ODLayout layout = new ODLayout();

            layout.addColumn(new Column(10,LengthType.PIXEL));
            layout.addColumn(new Column(95,LengthType.PIXEL));
            layout.addColumn(new Column(10,LengthType.PIXEL));
            layout.addColumn(new Column(95,LengthType.PIXEL));
            layout.addColumn(new Column(10,LengthType.PIXEL));
            layout.addColumn(new Column(Lengths.VARIABLE));
            layout.addColumn(new Column(10,LengthType.PIXEL));
            layout.addRow(new Row(10,LengthType.PIXEL));
            layout.addRow(new Row(30,LengthType.PIXEL));
            layout.addRow(new Row(10,LengthType.PIXEL));
            layout.addRow(new Row(Lengths.VARIABLE));
            layout.addRow(new Row(10,LengthType.PIXEL));
            layout.addRow(new Row(30,LengthType.PIXEL));
            layout.addRow(new Row(10,LengthType.PIXEL));

            JLabel selectProfile = new JLabel("<html><u>Select Profile</u></html>");
            JScrollPane profileScrollPane = new JScrollPane(profileJList);
            JButton createNewProfile = new JButton("Create");
            JButton deleteProfile = new JButton("Delete");
            JPanel editPanel = new JPanel();

            profileJList.setCellRenderer(new ProfileRenderer());

            selectProfile.setFont(new Font("Arial",Font.PLAIN,20));

            selectProfile.setHorizontalAlignment(JLabel.CENTER);

            {
                ODLayout editLayout = new ODLayout();

                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(Lengths.VARIABLE));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(200,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));

                editLayout.addColumn(new Column(10,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));

                JLabel gamesLabel = new JLabel("Game: ");
                JLabel versionsLabel = new JLabel("Version: ");
                JLabel filesLabel = new JLabel("File: ");

                JScrollPane gamesScroll = new JScrollPane(games);
                JScrollPane versionsScroll = new JScrollPane(versions);
                JScrollPane filesScroll = new JScrollPane(files);

                editPanel.add(gamesScroll);
                editPanel.add(gamesLabel);
                editPanel.add(versionsScroll);
                editPanel.add(versionsLabel);
                editPanel.add(filesScroll);
                editPanel.add(filesLabel);

                editLayout.registerComponent(gamesLabel,new Position(1,0));
                editLayout.registerComponent(gamesScroll,new Position(1,2));
                editLayout.registerComponent(versionsLabel,new Position(3,0));
                editLayout.registerComponent(versionsScroll,new Position(3,2));
                editLayout.registerComponent(filesLabel,new Position(5,0));
                editLayout.registerComponent(filesScroll,new Position(5,2));

                editPanel.setLayout(editLayout);
            }

            panel.add(selectProfile);
            panel.add(profileScrollPane);
            panel.add(createNewProfile);
            panel.add(deleteProfile);
            panel.add(editPanel);

            layout.registerComponent(selectProfile,new Position(1,1),new Position(3,1));
            layout.registerComponent(profileScrollPane,new Position(1,3),new Position(3,3));
            layout.registerComponent(createNewProfile,new Position(1,5));
            layout.registerComponent(deleteProfile,new Position(3,5));
            layout.registerComponent(editPanel,new Position(5,3),new Position(5,5));

            createNewProfile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("Create Profile");
                    profilesModel.add(0,new Profile(null,"FNaF","v1.3.0-alpha.1","New Profile"));
                    System.out.println("Created Profile");
                }
            });

            deleteProfile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    profilesModel.removeElementAt(profiles.getSelectedIndex());
                }
            });

            panel.setLayout(layout);

            tabbedPane.addTab("Profiles", panel);
        }

        {
            JPanel base = new JPanel();

            ODLayout baseLayout = new ODLayout();

            baseLayout.addRow(new Row(100,LengthType.PIXEL));
            baseLayout.addRow(new Row(130,LengthType.PIXEL));
            baseLayout.addRow(new Row(170,LengthType.PIXEL));
            baseLayout.addRow(new Row(170,LengthType.PIXEL));
            baseLayout.addRow(new Row(90,LengthType.PIXEL));
            baseLayout.addRow(new Row(Lengths.VARIABLE));
            baseLayout.addColumn(new Column(Lengths.VARIABLE));

            base.setLayout(baseLayout);

            {
                /**
                 * Top
                 */

                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                layout.addRow(new Row(Lengths.VARIABLE));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));

                JLabel loginTitle = new JLabel("Login");

                loginTitle.setFont(new Font("Arial",Font.PLAIN,30));
                loginTitle.setHorizontalAlignment(JLabel.CENTER);

                loggedInAs.setFont(new Font("Arial",Font.PLAIN,20));
                loggedInAs.setHorizontalAlignment(JLabel.CENTER);

                panel.add(loggedInAs);
                panel.add(loginTitle);

                layout.registerComponent(loggedInAs,new Position(0,0));
                layout.registerComponent(loginTitle,new Position(0,2));

                panel.setLayout(layout);

                base.add(panel);
                baseLayout.registerComponent(panel,new Position(0,0));
            }

            {
                /**
                 * Middle
                 */
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                layout.addRow(new Row(10, LengthType.PIXEL));
                layout.addRow(new Row(30, LengthType.PIXEL));
                layout.addRow(new Row(10, LengthType.PIXEL));
                layout.addRow(new Row(30, LengthType.PIXEL));
                layout.addRow(new Row(10, LengthType.PIXEL));
                layout.addRow(new Row(30, LengthType.PIXEL));
                layout.addRow(new Row(10, LengthType.PIXEL));
                layout.addRow(new Row(Lengths.VARIABLE));

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(200,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                JLabel usernameLabel = new JLabel("Username:");
                JLabel password = new JLabel("Password:");

                panel.add(usernameLabel);
                panel.add(password);
                panel.add(usernameField);
                panel.add(passwordField);

                usernameLabel.setFont(new Font("Arial",Font.PLAIN,20));
                password.setFont(new Font("Arial",Font.PLAIN,20));
                usernameField.setFont(new Font("Arial",Font.PLAIN,20));
                passwordField.setFont(new Font("Arial",Font.PLAIN,20));

                layout.registerComponent(usernameLabel,new Position(1,3));
                layout.registerComponent(password,new Position(1,5));
                layout.registerComponent(usernameField,new Position(2,3));
                layout.registerComponent(passwordField,new Position(2,5));

                panel.setLayout(layout);

                base.add(panel);
                baseLayout.registerComponent(panel,new Position(0,1));
            }
            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(50,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                JLabel createAccountLabel = new JLabel("Create Account");

                createAccountLabel.setFont(new Font("Arial",Font.PLAIN,30));
                createAccountLabel.setHorizontalAlignment(JLabel.CENTER);

                panel.add(loginButton);
                panel.add(logoutButton);
                panel.add(createAccountLabel);

                layout.registerComponent(loginButton,new Position(1,1));
                layout.registerComponent(logoutButton,new Position(1,3));
                layout.registerComponent(createAccountLabel,new Position(1,5));

                panel.setLayout(layout);

                base.add(panel);

                baseLayout.registerComponent(panel,new Position(0,2));
            }
            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(200,LengthType.PIXEL));
                layout.addColumn(new Column(20,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                JLabel usernameCreateLabel = new JLabel("Username:");
                JLabel displaynameCreateLabel = new JLabel("Display Name:");
                JLabel passwordCreateLabel = new JLabel("Password:");
                JLabel passwordCreateConfirmLabel = new JLabel("Confirm Password:");

                panel.add(usernameCreation);
                panel.add(displayNameCreation);
                panel.add(passwordCreation);
                panel.add(passwordCreationConfirm);

                panel.add(usernameCreateLabel);
                panel.add(displaynameCreateLabel);
                panel.add(passwordCreateLabel);
                panel.add(passwordCreateConfirmLabel);

                usernameCreateLabel.setFont(new Font("Arial",Font.PLAIN,20));
                displaynameCreateLabel.setFont(new Font("Arial",Font.PLAIN,20));
                passwordCreateLabel.setFont(new Font("Arial",Font.PLAIN,20));
                passwordCreateConfirmLabel.setFont(new Font("Arial",Font.PLAIN,20));

                layout.registerComponent(usernameCreation,new Position(3,1));
                layout.registerComponent(displayNameCreation,new Position(3,3));
                layout.registerComponent(passwordCreation,new Position(3,5));
                layout.registerComponent(passwordCreationConfirm,new Position(3,7));

                layout.registerComponent(usernameCreateLabel,new Position(1,1));
                layout.registerComponent(displaynameCreateLabel,new Position(1,3));
                layout.registerComponent(passwordCreateLabel,new Position(1,5));
                layout.registerComponent(passwordCreateConfirmLabel,new Position(1,7));

                panel.setLayout(layout);

                base.add(panel);

                baseLayout.registerComponent(panel,new Position(0,3));
            }
            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                actionInfo.setFont(new Font("Arial",Font.PLAIN,20));

                panel.add(createButton);
                panel.add(actionInfo);

                layout.registerComponent(createButton,new Position(1,1));
                layout.registerComponent(actionInfo,new Position(1,3));

                panel.setLayout(layout);

                base.add(panel);

                baseLayout.registerComponent(panel,new Position(0,4));
            }
            tabbedPane.addTab("Login", base);
        }

        {
            JPanel panel = new JPanel();

            ODLayout layout = new ODLayout();

            tabbedPane.addTab("Acccount", panel);
        }

        {
            /**
             * Settings
             */
            JPanel settingsTab = new JPanel();

            tabbedPane.addTab("Settings", settingsTab);
        }

        setContentPane(tabbedPane);

        setVisible(true);

        profileJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                System.out.println("Event");
                gamesModel.removeAllElements();
                gamesModel.addAll(CacheRequest.getGames());
                if(gamesModel.contains(profileJList.getSelectedValue().getGame())) games.setSelectedValue(profileJList.getSelectedValue().getGame(),false);
                else games.setSelectedIndex(0);
            }
        });

        games.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                versionsModel.removeAllElements();
                versionsModel.addAll(CacheRequest.getVersions(games.getSelectedValue()));
                if(versionsModel.contains(profileJList.getSelectedValue().getVersion())) versions.setSelectedValue(profileJList.getSelectedValue().getVersion(),false);
                else versions.setSelectedIndex(0);
            }
        });

        versions.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                filesModel.removeAllElements();
                filesModel.addAll(CacheRequest.getFiles(games.getSelectedValue(),versions.getSelectedValue()));
                if(filesModel.contains(profileJList.getSelectedValue().getFile())) files.setSelectedValue(profileJList.getSelectedValue().getFile(),false);
                else files.setSelectedIndex(0);
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!authenticatorClient.isConnected()){
                    try {
                        authenticatorClient.setPort(Utilities.getIPData().getInt("auth-server-port"));
                        authenticatorClient.setAddress(Utilities.getIPData().getString("auth-server"));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
                    }
                    authenticatorClient.setup();
                    if(!authenticatorClient.isConnected()){
                        JOptionPane.showMessageDialog(main.getContentPane(),"Couldn't connect to auth Server. Please try again later.");
                        return;
                    }
                }

                JSONObject result = authenticatorClient.loginAccount(usernameField.getText(),new String(passwordField.getPassword()));

                switch (result.getString("data")) {
                    case "account-doesnt-exist" -> {
                        actionInfo.setText("The account '" + usernameField.getText() + "' doesn't exist.");
                        actionInfo.setForeground(Color.RED);
                    }
                    case "login-success" -> {
                        isLoggedIn = true;
                        TOKEN = result.getString("token");
                        result = authenticatorClient.tokeninfo(TOKEN);
                        displayname = result.getString("displayname");
                        username = result.getString("username");
                        loggedInAs.setText("Logged in as: " + displayname);
                        actionInfo.setText("Login success.");
                        actionInfo.setForeground(Color.GREEN);
                    }
                    case "incorrect-password" -> {
                        actionInfo.setText("Password incorrect.");
                        actionInfo.setForeground(Color.RED);
                    }
                }
                config.put("token",TOKEN);
                try {
                    CacheServer.writeStringToFile(config,configSaveFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!authenticatorClient.isConnected()){
                    try {
                        authenticatorClient.setPort(Utilities.getIPData().getInt("auth-server-port"));
                        authenticatorClient.setAddress(Utilities.getIPData().getString("auth-server"));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
                    }
                    authenticatorClient.setup();
                    if(!authenticatorClient.isConnected()){
                        JOptionPane.showMessageDialog(main.getContentPane(),"Couldn't connect to auth Server. Please try again later.");
                        return;
                    }
                }

                JSONObject result = authenticatorClient.createAccount(usernameCreation.getText(), displayNameCreation.getText(),new String(passwordCreation.getPassword()));

                switch (result.getString("data")) {
                    case "account-created" -> {
                        actionInfo.setText("The account '" + usernameCreation.getText() + "' has been created.");
                        actionInfo.setForeground(Color.GREEN);
                    }
                    case "account-already-exists" -> {
                        actionInfo.setText("The account '" + usernameCreation.getText() + "' already exists.");
                        actionInfo.setForeground(Color.RED);
                    }
                }

                System.out.println(result);
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TOKEN = "";
                isLoggedIn = false;
                loggedInAs.setText("Not logged in");
                actionInfo.setText("Logged out.");
                actionInfo.setForeground(Color.RED);
                config.put("token",TOKEN);
                try {
                    CacheServer.writeStringToFile(config,configSaveFile);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        Thread updater = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if(isLoggedIn){
                        loginButton.setEnabled(false);
                        logoutButton.setEnabled(true);

                        usernameField.setEnabled(false);
                        passwordField.setEnabled(false);

                        usernameCreation.setEnabled(false);
                        displayNameCreation.setEnabled(false);
                        passwordCreation.setEnabled(false);
                        passwordCreationConfirm.setEnabled(false);
                        createButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                        logoutButton.setEnabled(false);

                        usernameField.setEnabled(true);
                        passwordField.setEnabled(true);

                        usernameCreation.setEnabled(true);
                        displayNameCreation.setEnabled(true);
                        passwordCreation.setEnabled(true);
                        passwordCreationConfirm.setEnabled(true);
                        createButton.setEnabled(true);
                    }
                    try {
                        if(profiles.getSelectedIndex() != -1){
                            info.setText(Util.convertMarkdownToHtml(GitHub.fetchReadmeContent(profiles.getSelectedValue().getAuthor(),profiles.getSelectedValue().getGame())));
                        } else {
                            info.setText(Util.convertMarkdownToHtml("# No Profile Selected"));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    JSONObject profilesObject = new JSONObject();

                    Enumeration<Profile> profileEnumeration = profilesModel.elements();

                    while (profileEnumeration.hasMoreElements()) {
                        Profile p = profileEnumeration.nextElement();

                        if(games.getSelectedIndex() != -1) p.setGame(games.getSelectedValue());
                        if(versions.getSelectedIndex() != -1) p.setVersion(versions.getSelectedValue());
                        if(files.getSelectedIndex() != -1) p.setFile(files.getSelectedValue());

                        profilesObject.put(p.getUuid(),p.asJSON());
                    }

                    config.put("profiles",profilesObject);

                    try {
                        CacheServer.writeStringToFile(config,configSaveFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        updater.start();

        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(() -> {
                    try{
                        launch.setEnabled(false);

                        Profile p = profiles.getSelectedValue();
                        if(p == null){
                            JOptionPane.showMessageDialog(main,"Please select a Profile first.");
                            launch.setEnabled(true);
                            return;
                        }

                        String destination = "files/" + p.getAuthor() + "/" + p.getGame() + "/" + p.getVersion() + "/" + p.getFile();

                        File saveLocaton = new File(destination);

                        if(!saveLocaton.exists()) {
                            FileDownloader.downloadFile("https://github.com/" + p.getAuthor() + "/" + p.getGame() + "/releases/download/" + p.getVersion() + "/" +p.getFile(), destination, status);
                            while (!status.isComplete()) {
                                progress.setMaximum((int) status.getBytesTotal());
                                progress.setValue((int) status.getBytesRead());
                                progress.setString("Downloading " + String.format("%.2f",status.getBytesRead() / 1024f / 1024f) + "MB / " + String.format("%.2f",status.getBytesTotal() / 1024f / 1024f) + " MB (" + String.format("%.2f%%", ((float) status.getBytesRead() / (float) status.getBytesTotal()) * 100f) + ")");
                            }
                        } else {
                            status.setComplete(true);
                        }
                        try {
                            Process pr = Runtime.getRuntime().exec("java -jar " + destination + " " + TOKEN);
                        } catch (IOException ex) {
                            launch.setEnabled(true);
                            throw new RuntimeException(ex);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        launch.setEnabled(true);
                    }
                });
                t.start();
            }
        });

        /*launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if(versions.getSelectedValue() != null && games.getSelectedValue() != null) {
                        String destination ="files/Redstoner-2019/" + games.getSelectedValue() + "/" + versions.getSelectedValue() + "/" + files.getSelectedValue();

                        if(!new File(destination).exists()) {
                            FileDownloader.downloadFile("https://github.com/" + authors.getSelectedValue() + "/" + games.getSelectedValue() + "/releases/download/" + versions.getSelectedValue() + "/" + files.getSelectedValue(), destination, status);
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
        });*/

        //Setup complete

        /*try{
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

                System.out.println("Auth Client Connected");
            }
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't connect to auth Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }

        try{
            if(!offlineMode){
                statsClient = new StatisticClient(onlineData.getString("statistics-server"),onlineData.getInt("statistics-server-port"));
            }
        }catch (Exception ignored){
            offlineMode = true;
            JOptionPane.showMessageDialog(this,"Couldn't connect to auth Server. Switching to offline mode.","Error",JOptionPane.ERROR_MESSAGE);
        }*/

        /*Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try{
                        JSONObject gamesObject = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue());
                        for(String s : gamesObject.keySet()){
                            if(!gamesListModel.contains(s)){
                                gamesListModel.add(0,s);
                            }
                        }
                        Enumeration<String> enumeration = gamesListModel.elements();
                        while (enumeration.hasMoreElements()) {
                            String s = enumeration.nextElement();
                            if(!gamesObject.has(s)) gamesListModel.removeElement(s);
                        }

                        JSONObject versionsObject = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue()).getJSONObject(games.getSelectedValue());
                        for(String s : versionsObject.keySet()){
                            if(!versionModel.contains(s)){
                                versionModel.add(0,s);
                            }
                        }
                        enumeration = versionModel.elements();
                        while (enumeration.hasMoreElements()) {
                            String s = enumeration.nextElement();
                            if(!versionsObject.has(s)) versionModel.removeElement(s);
                        }

                        JSONObject assets = cacheData.getJSONObject("repos").getJSONObject(authors.getSelectedValue()).getJSONObject(games.getSelectedValue()).getJSONObject(versions.getSelectedValue());
                        for(String s : assets.keySet()){
                            if(!filesModel.contains(s)){
                                filesModel.add(0,s);
                            }
                        }
                        enumeration = filesModel.elements();
                        while (enumeration.hasMoreElements()) {
                            String s = enumeration.nextElement();
                            if(!assets.has(s)) filesModel.removeElement(s);
                        }
                    }catch (Exception e){}

                    if(authors.getSelectedIndex() == -1) authors.setSelectedIndex(0);
                    if(games.getSelectedIndex() == -1) games.setSelectedIndex(0);
                    if(versions.getSelectedIndex() == -1) versions.setSelectedIndex(0);
                    if(files.getSelectedIndex() == -1) files.setSelectedIndex(0);
                }
            }
        });*/
        //updateThread.start();

        /*Thread console = new Thread(new Runnable() {
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
        console.start();*/

        /*while (true){
            downloadProgress.setValue((int) status.getBytesRead());
            downloadProgress.setMaximum((int) status.getBytesTotal());

            downloadProgress.setString(String.format("%d / %d (%.2f%%) - Status: %d, Complete: %s",downloadProgress.getValue(),downloadProgress.getMaximum(),((float) downloadProgress.getValue() / (float) downloadProgress.getMaximum()) * 100, status.getStatus(), status.isComplete() + ""));
        }*/
    }
    public static void main(String[] args) throws Exception {
        File file = new File("odlauncher/config.json");
        if(!file.exists()){
            file.getParentFile().mkdirs();
            file.createNewFile();
            JSONObject config = new JSONObject();
            config.put("token","");
            config.put("profiles",new JSONObject());
            CacheServer.writeStringToFile(config,file);
        }
        new Main();
    }

    /*public JSONObject runCacheCommand(JSONObject request){
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
    }*/

    public static void printStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();

        // Start from index 1 to skip the getStackTrace() method itself
        for (int i = 1; i < stackTraceElements.length; i++) {
            System.out.println(stackTraceElements[i]);
        }
    }

    static class ProfileRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Profile profile = (Profile) value;

            JPanel panel = new JPanel();

            if(isSelected){
                panel.setBackground(Color.LIGHT_GRAY);
            }

            FontMetrics fm = panel.getFontMetrics(panel.getFont());

            try{
                panel.setPreferredSize(new Dimension(Math.max(fm.stringWidth(profile.getGame()),fm.stringWidth(profile.getVersion())) + 100,60));
            } catch (Exception e){
                panel.setPreferredSize(new Dimension(200,60));
                e.printStackTrace();
            }

            ODLayout layout = new ODLayout();

            layout.addColumn(new Column(60,LengthType.PIXEL));
            layout.addColumn(new Column(10,LengthType.PIXEL));
            layout.addColumn(new Column(Lengths.VARIABLE));
            layout.addRow(new Row(20,LengthType.PIXEL));
            layout.addRow(new Row(20,LengthType.PIXEL));
            layout.addRow(new Row(20,LengthType.PIXEL));

            JLabel image = new JLabel();
            JLabel name = new JLabel(profile.getName());
            JLabel game = new JLabel(profile.getGame());
            JLabel version = new JLabel(profile.getVersion());

            if(profile.getImage() == null){
                BufferedImage bf = new BufferedImage(60,60,1);
                Graphics2D g = bf.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0,0,60,60);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial",Font.PLAIN,60));
                g.drawString("?",15,52);
                g.dispose();
                profile.setImage(bf);
            }

            image.setIcon(new ImageIcon(profile.getImage()));

            panel.add(image);
            panel.add(game);
            panel.add(version);
            panel.add(name);

            layout.registerComponent(image,new Position(0,0),new Position(0,2));
            layout.registerComponent(name,new Position(2,0));
            layout.registerComponent(game,new Position(2,1));
            layout.registerComponent(version,new Position(2,2));

            panel.setLayout(layout);

            return panel;
        }
    }
}