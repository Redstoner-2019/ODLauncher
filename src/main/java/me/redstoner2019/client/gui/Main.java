package me.redstoner2019.client.gui;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.IntelliJTheme;
import me.redstoner2019.*;
import me.redstoner2019.ODLayout;
import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.github.CacheRequest;
import me.redstoner2019.client.github.GitHub;
import me.redstoner2019.client.request.Requests;
import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;
import raven.toast.Notifications;

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
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

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

    private DefaultListModel<Profile> profilesModel = new DefaultListModel<>();
    private JList<Profile> profiles = new JList<>(profilesModel);
    private JList<Profile> profileJList = new JList<>(profilesModel);
    private JScrollPane scrollPane = new JScrollPane(profiles);
    private JLabel info = new JLabel();
    private JScrollPane versionInfo = new JScrollPane(info);
    private JProgressBar progress = new JProgressBar();
    private JButton launch = new JButton("Launch");
    private JButton downloadFile = new JButton("Download");
    private JTextField profileName = new JTextField();
    private DefaultListModel<String> gamesModel = new DefaultListModel<>();
    private DefaultListModel<String> versionsModel = new DefaultListModel<>();
    private DefaultListModel<String> filesModel = new DefaultListModel<>();
    private JList<String> games = new JList<>(gamesModel);
    private JList<String> versions = new JList<>(versionsModel);
    private JList<String> files = new JList<>(filesModel);
    private static JLabel image = new JLabel();
    private File configSaveFile = new File("odlauncher/config.json");
    private JSONObject config;

    public Main() throws IOException {
        main = this;

        SwingUtilities.updateComponentTreeUI(this);

        configSaveFile = new File("odlauncher/config.json");

        config = new JSONObject(CacheServer.readFile(configSaveFile));

        if(config.has("token")) TOKEN = config.getString("token");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    authenticatorClient.setPort(Utilities.getIPData().getInt("auth-server-port"));
                    authenticatorClient.setAddress(Utilities.getIPData().getString("auth-server"));
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
                }
                authenticatorClient.setup();

                System.out.println("Connecting to " + authenticatorClient.getAddress() + ":" + authenticatorClient.getPort());
            }
        });
        t.start();

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

        profiles.setCellRenderer(new ProfileRenderer());

        if(config.has("profiles")){
            JSONObject o = config.getJSONObject("profiles");
            for(String uuid : o.keySet()){
                profilesModel.add(0,new Profile().fromJSON(o.getJSONObject(uuid)));
            }
        }

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
        JTextField email = new JTextField();
        JPasswordField passwordCreation = new JPasswordField();

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

        try{
            JSONObject request = new JSONObject();
            request.put("token",TOKEN);
            JSONObject result = Requests.request("http://158.220.105.209:8080/verifyToken",request);

            if(result.getInt("status") == 0){
                request = new JSONObject();
                request.put("token",TOKEN);
                result = Requests.request("http://158.220.105.209:8080/tokenInfo",request);

                displayname = result.getString("displayname");
                username = result.getString("username");
                loggedInAs.setText("Logged in as: " + displayname);
                actionInfo.setText("Login success.");
                actionInfo.setForeground(Color.GREEN);
                isLoggedIn = true;
            }
        }catch (Exception e){
            e.printStackTrace();
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
            progress.setString("No Download Running");
            progress.setFont(new Font("Arial",Font.PLAIN,12));

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
            bottomLayout.addColumn(new Column(200,LengthType.PIXEL));
            bottomLayout.addColumn(new Column(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(Lengths.VARIABLE));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));
            bottomLayout.addRow(new Row(Lengths.VARIABLE));
            bottomLayout.addRow(new Row(10,LengthType.PIXEL));

            bottomPanel.add(launch);
            bottomPanel.add(downloadFile);
            bottomPanel.add(progress);

            bottomLayout.registerComponent(launch,new Position(1,1));
            bottomLayout.registerComponent(downloadFile,new Position(3,1));
            bottomLayout.registerComponent(progress,new Position(1,3),new Position(3,3));

            bottomPanel.setLayout(bottomLayout);
            panel.add(bottomPanel);

            layout.registerComponent(topPanel,new Position(0,0));
            layout.registerComponent(bottomPanel,new Position(0,1));
            panel.setLayout(layout);
            tabbedPane.addTab("Launcher", panel);
        }
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
                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(30,LengthType.PIXEL));
                editLayout.addRow(new Row(10,LengthType.PIXEL));
                editLayout.addRow(new Row(30,LengthType.PIXEL));

                editLayout.addColumn(new Column(0,LengthType.PIXEL));
                editLayout.addColumn(new Column(100,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));
                editLayout.addColumn(new Column(100,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));
                editLayout.addColumn(new Column(100,LengthType.PIXEL));
                editLayout.addColumn(new Column(Lengths.VARIABLE));
                editLayout.addColumn(new Column(10,LengthType.PIXEL));

                JLabel gamesLabel = new JLabel("Game");
                JLabel versionsLabel = new JLabel("Version");
                JLabel filesLabel = new JLabel("File");
                JLabel profileNameLabel = new JLabel("Profile Name: ");

                JScrollPane gamesScroll = new JScrollPane(games);
                JScrollPane versionsScroll = new JScrollPane(versions);
                JScrollPane filesScroll = new JScrollPane(files);
                JButton saveProfiles = new JButton("Save");

                JButton loadImage = new JButton("Load Icon...");

                gamesLabel.setFont(new Font("Arial",Font.PLAIN,20));
                versionsLabel.setFont(new Font("Arial",Font.PLAIN,20));
                filesLabel.setFont(new Font("Arial",Font.PLAIN,20));

                gamesLabel.setHorizontalAlignment(JLabel.CENTER);
                versionsLabel.setHorizontalAlignment(JLabel.CENTER);
                filesLabel.setHorizontalAlignment(JLabel.CENTER);

                editPanel.add(gamesScroll);
                editPanel.add(gamesLabel);
                editPanel.add(versionsScroll);
                editPanel.add(versionsLabel);
                editPanel.add(filesScroll);
                editPanel.add(filesLabel);
                editPanel.add(profileNameLabel);
                editPanel.add(profileName);
                editPanel.add(saveProfiles);
                editPanel.add(loadImage);

                editLayout.registerComponent(gamesLabel,new Position(1,0),new Position(2,0));
                editLayout.registerComponent(gamesScroll,new Position(1,2),new Position(2,2));
                editLayout.registerComponent(versionsLabel,new Position(4,0),new Position(5,0));
                editLayout.registerComponent(versionsScroll,new Position(4,2),new Position(5,2));
                editLayout.registerComponent(filesLabel,new Position(7,0),new Position(8,0));
                editLayout.registerComponent(filesScroll,new Position(7,2),new Position(8,2));
                editLayout.registerComponent(profileNameLabel,new Position(1,4));
                editLayout.registerComponent(loadImage,new Position(1,6));
                editLayout.registerComponent(profileName,new Position(2,4));
                editLayout.registerComponent(saveProfiles,new Position(1,12));

                saveProfiles.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        saveAllProfiles();
                    }
                });

                loadImage.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Load Start");
                        if(profileJList.getSelectedValue() == null) return;
                        JFileChooser chooser = new JFileChooser();
                        int result = chooser.showOpenDialog(main);
                        switch (result) {
                            case JFileChooser.APPROVE_OPTION -> {
                                File file = chooser.getSelectedFile();
                                profileJList.getSelectedValue().setIcon(file.getAbsolutePath());
                                try {
                                    image.setIcon(new ImageIcon(ImageIO.read(new File(profileJList.getSelectedValue().getIcon()))));
                                } catch (Exception ex) {
                                    BufferedImage bf = new BufferedImage(60,60,1);
                                    Graphics2D g = bf.createGraphics();
                                    g.setColor(Color.WHITE);
                                    g.fillRect(0,0,60,60);
                                    g.setColor(Color.RED);
                                    g.setFont(new Font("Arial",Font.PLAIN,60));
                                    g.drawString("?",15,52);
                                    g.dispose();
                                    image.setIcon(new ImageIcon(bf));
                                    image.repaint();
                                    image.revalidate();
                                }
                            }
                            case JFileChooser.CANCEL_OPTION -> {

                            }
                            case JFileChooser.ERROR_OPTION -> {

                            }
                        }
                        System.out.println("Load End");
                    }
                });

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
                    profilesModel.removeElementAt(profileJList.getSelectedIndex());
                    saveAllProfiles();
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
                panel.add(email);

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
                layout.registerComponent(email,new Position(3,5));
                layout.registerComponent(passwordCreation,new Position(3,7));

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

        {
            JTabbedPane developerPanel = new JTabbedPane();

            {
                JPanel panel = new JPanel();

                JTextField txtGameName = new JTextField();
                JTextField txtGameID = new JTextField();
                JFormattedTextField fmtDescription = new JFormattedTextField();

                JLabel lblGameName = new JLabel("Game Name:");
                JLabel lblGameID = new JLabel("Game ID:");
                JLabel lblDescription = new JLabel("Game Description:");

                JButton btnCreate = new JButton("Create");

                panel.add(txtGameName);
                panel.add(txtGameID);
                panel.add(fmtDescription);

                panel.add(lblGameName);
                panel.add(lblGameID);
                panel.add(lblDescription);

                panel.add(btnCreate);

                ODLayout layout = new ODLayout();

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(100,LengthType.PIXEL));
                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(Lengths.VARIABLE));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));

                layout.registerComponent(lblGameName,new Position(1,1));
                layout.registerComponent(txtGameName,new Position(3,1));
                layout.registerComponent(lblGameID,new Position(1,3));
                layout.registerComponent(txtGameID,new Position(3,3));
                layout.registerComponent(lblDescription,new Position(1,5),new Position(3,5));
                layout.registerComponent(fmtDescription,new Position(1,7),new Position(3,7));
                layout.registerComponent(btnCreate,new Position(1,9));

                panel.setLayout(layout);

                developerPanel.addTab("Create Game", panel);

                btnCreate.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Create Game");
                    }
                });
            }

            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                JLabel lblGameID = new JLabel("Game ID:");
                JTextField txtGameID = new JTextField();
                JSeparator sepGameID = new JSeparator();
                JLabel lblGameName = new JLabel("Game Name:");
                JTextField txtGameName = new JTextField();
                JSeparator sepGameName = new JSeparator();
                JButton btnUpdateGameName = new JButton("Update Game Name");

                panel.add(lblGameID);
                panel.add(txtGameID);
                panel.add(sepGameID);
                panel.add(lblGameName);
                panel.add(txtGameName);
                panel.add(btnUpdateGameName);
                panel.add(sepGameName);

                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(100,LengthType.PIXEL));
                layout.addColumn(new Column(10,LengthType.PIXEL));
                layout.addColumn(new Column(Lengths.VARIABLE));
                layout.addColumn(new Column(10,LengthType.PIXEL));

                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));
                layout.addRow(new Row(30,LengthType.PIXEL));
                layout.addRow(new Row(10,LengthType.PIXEL));

                layout.registerComponent(lblGameID,new Position(1,1));
                layout.registerComponent(txtGameID,new Position(3,1));
                layout.registerComponent(sepGameID,new Position(0,3),new Position(4,3));
                layout.registerComponent(lblGameName,new Position(1,5));
                layout.registerComponent(txtGameName,new Position(3,5));
                layout.registerComponent(btnUpdateGameName,new Position(1,7));
                layout.registerComponent(sepGameName,new Position(0,9),new Position(4,9));

                panel.setLayout(layout);

                developerPanel.addTab("Manage Game", panel);
            }

            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                panel.setLayout(layout);

                developerPanel.addTab("Upload new Version", panel);
            }

            {
                JPanel panel = new JPanel();

                ODLayout layout = new ODLayout();

                panel.setLayout(layout);

                developerPanel.addTab("Manage Game Challenges", panel);
            }

            tabbedPane.addTab("Developer Settings", developerPanel);
        }

        setContentPane(tabbedPane);

        setVisible(true);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if(tabbedPane.getSelectedIndex() == 5 && !isLoggedIn){
                    tabbedPane.setSelectedIndex(2);
                    JOptionPane.showMessageDialog(null, "You are not logged in!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        profileJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(profileJList.getSelectedIndex() == -1){
                            if(profilesModel.isEmpty()) createButton.getActionListeners()[0].actionPerformed(null);
                            profileJList.setSelectedIndex(0);
                        }
                        gamesModel.removeAllElements();
                        gamesModel.addAll(CacheRequest.getGames());
                        if(gamesModel.contains(profileJList.getSelectedValue().getGame())) games.setSelectedValue(profileJList.getSelectedValue().getGame(),false);
                        else games.setSelectedIndex(0);
                        profileName.setText(profileJList.getSelectedValue().getName());
                    }
                });
                t.start();
            }
        });

        games.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(profileJList.getSelectedIndex() == -1){
                            if(profilesModel.isEmpty()) createButton.getActionListeners()[0].actionPerformed(null);
                            profileJList.setSelectedIndex(0);
                        }
                        versionsModel.removeAllElements();
                        versionsModel.addAll(CacheRequest.getVersions(games.getSelectedValue()));
                        if(versionsModel.contains(profileJList.getSelectedValue().getVersion())) versions.setSelectedValue(profileJList.getSelectedValue().getVersion(),false);
                        else versions.setSelectedIndex(0);
                    }
                });
                t.start();
            }
        });

        versions.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(profileJList.getSelectedIndex() == -1){
                            if(profilesModel.isEmpty()) createButton.getActionListeners()[0].actionPerformed(null);
                            profileJList.setSelectedIndex(0);
                        }
                        filesModel.removeAllElements();
                        filesModel.addAll(CacheRequest.getFiles(games.getSelectedValue(),versions.getSelectedValue()));
                        if(filesModel.contains(profileJList.getSelectedValue().getFile())) files.setSelectedValue(profileJList.getSelectedValue().getFile(),false);
                        else files.setSelectedIndex(0);
                    }
                });
                t.start();
            }
        });

        JFrame frame = this;

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Test");
                JSONObject result;
                try {
                    JSONObject request = new JSONObject();
                    request.put("username",usernameField.getText());
                    request.put("password",new String(passwordField.getPassword()));
                    result = Requests.request("http://158.220.105.209:8080/login",request);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
                    return;
                }

                switch (result.getString("message")) {
                    case "user-not-found" -> {
                        actionInfo.setText("The account '" + usernameField.getText() + "' doesn't exist.");
                        actionInfo.setForeground(Color.RED);
                        //Notification.notification("ODLauncher","This account doesn't exist.");
                        Notifications.getInstance().setJFrame(frame);
                        Notifications.getInstance().show(Notifications.Type.ERROR,Notifications.Location.TOP_CENTER,"This account doesn't exist.");
                        Notifications.getInstance().setJFrame(null);
                    }
                    case "success" -> {
                        isLoggedIn = true;
                        TOKEN = result.getString("token");

                        JSONObject request = new JSONObject();
                        request.put("token",TOKEN);

                        result = Requests.request("http://158.220.105.209:8080/tokenInfo",request);
                        displayname = result.getString("displayname");
                        username = result.getString("username");
                        loggedInAs.setText("Logged in as: " + displayname);
                        actionInfo.setText("Login success.");
                        actionInfo.setForeground(Color.GREEN);
                        //Notification.notification("ODLauncher","Login success.");
                        Notifications.getInstance().setJFrame(frame);
                        Notifications.getInstance().show(Notifications.Type.SUCCESS,Notifications.Location.TOP_CENTER,"Login success.");
                        Notifications.getInstance().setJFrame(null);
                    }
                    case "incorrect-password" -> {
                        actionInfo.setText("Password incorrect.");
                        actionInfo.setForeground(Color.RED);
                        //Notification.notification("ODLauncher","Incorrect password.");
                        Notifications.getInstance().setJFrame(frame);
                        Notifications.getInstance().show(Notifications.Type.ERROR,Notifications.Location.TOP_CENTER,"Incorrect password.");
                        Notifications.getInstance().setJFrame(null);
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

        profiles.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(profiles.getSelectedIndex() != -1){
                                Profile p = profiles.getSelectedValue();

                                System.out.println("http://" + /*Utilities.getIPData().getString("cache-server") +*/ "/api/" + p.getGame() + "/" + p.getVersion());

                                JSONObject fileInfo = new JSONObject(new String(new URL("http://" + Utilities.getIPData().getString("cache-server") + "/api/" + p.getGame() + "/" + p.getVersion()).openConnection().getInputStream().readAllBytes())).getJSONObject(p.getFile());
                                String infoText = "# File Info: \n ## Filename:      " + p.getFile() +
                                        "\n ## Download URL:  " + fileInfo.getString("browser_download_url") +
                                        "\n ## Size:          " + String.format("%.2f",fileInfo.getInt("size") / 1024f / 1024f) + " MB"+
                                        "\n ## Downloads:     " + fileInfo.getInt("download_count") +
                                        "\n";
                                infoText+=GitHub.fetchReadmeContent(profiles.getSelectedValue().getAuthor(),profiles.getSelectedValue().getGame());
                                info.setText(Util.convertMarkdownToHtml(infoText));
                            } else {
                                info.setText(Util.convertMarkdownToHtml("# No Profile Selected"));
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
                t.start();
            }
        });

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!authenticatorClient.isConnected()){
                    try {
                        authenticatorClient.setPort(Utilities.getIPData().getInt("auth-server-port"));
                        authenticatorClient.setAddress(Utilities.getIPData().getString("auth-server"));
                        System.out.println("Connecting to " + authenticatorClient.getAddress() + " " + authenticatorClient.getPort());
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(main.getContentPane(),"Failed to retrieve ip for the Authentication Server. Are you connected to the Internet?");
                    }
                    authenticatorClient.setup();
                    if(!authenticatorClient.isConnected()){
                        JOptionPane.showMessageDialog(main.getContentPane(),"Couldn't connect to auth Server. Please try again later.");
                        return;
                    }
                }

                System.out.println("Connection");

                JSONObject result = authenticatorClient.createAccount(usernameCreation.getText(), displayNameCreation.getText(),new String(passwordCreation.getPassword()),email.getText());
                System.out.println(result);

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
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TOKEN = "";
                isLoggedIn = false;
                loggedInAs.setText("Not logged in");
                actionInfo.setText("Logged out.");
                Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.TOP_CENTER,"Logged out.");
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
                        email.setEnabled(false);
                        createButton.setEnabled(false);
                    } else {
                        loginButton.setEnabled(true);
                        logoutButton.setEnabled(false);

                        usernameField.setEnabled(true);
                        passwordField.setEnabled(true);

                        usernameCreation.setEnabled(true);
                        displayNameCreation.setEnabled(true);
                        passwordCreation.setEnabled(true);
                        email.setEnabled(true);
                        createButton.setEnabled(true);
                    }
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        updater.start();

        downloadFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        downloadFile.setEnabled(false);
                        launch.setEnabled(false);

                        Profile p = profiles.getSelectedValue();
                        if(p == null){
                            JOptionPane.showMessageDialog(main,"Please select a Profile first.");
                            launch.setEnabled(true);
                            downloadFile.setEnabled(true);
                            return;
                        }

                        String destination = "files/" + p.getAuthor() + "/" + p.getGame() + "/" + p.getVersion() + "-" + p.getFile();

                        File saveLocaton = new File(destination);

                        System.out.println("Start download");

                        FileDownloader.downloadFile("https://github.com/" + p.getAuthor() + "/" + p.getGame() + "/releases/download/" + p.getVersion() + "/" +p.getFile(), destination, status);

                        long lastSpeedUpdate = System.currentTimeMillis();
                        long speed = 0;
                        long lastBytes = 0;

                        while (!status.isComplete()) {
                            if(System.currentTimeMillis() - lastSpeedUpdate > 1000){
                                lastSpeedUpdate = System.currentTimeMillis();
                                speed = status.getBytesRead() - lastBytes;
                                lastBytes = status.getBytesRead();
                            }
                            progress.setMaximum((int) status.getBytesTotal());
                            progress.setValue((int) status.getBytesRead());

                            String timeLeft = "Waiting...";

                            if(speed > 0){
                                long time = ((status.getBytesTotal() - status.getBytesRead()) / speed) * 1000;
                                timeLeft = Util.convertMillisToHMS(time);
                            }

                            progress.setString("Downloading " + String.format("%.2f",status.getBytesRead() / 1024f / 1024f) + "MB / " + String.format("%.2f",status.getBytesTotal() / 1024f / 1024f) + " MB (" + String.format("%.2f%%", ((float) status.getBytesRead() / (float) status.getBytesTotal()) * 100f) + "), " + String.format("%.2f",speed / 1024f / 1024f) + " MB/s, Time left: " + timeLeft);
                        }

                        //Notification.notification("ODLauncher","Completed download of " + p.getGame() + " - " + p.getVersion());
                        Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.BOTTOM_RIGHT,10000,"Completed download of " + p.getGame() + " - " + p.getVersion());
                        System.out.println("Download Complete");

                        downloadFile.setEnabled(true);
                        launch.setEnabled(true);
                        progress.setValue(100);
                        progress.setMaximum(100);

                        progress.setString("Download Complete (" + p.getGame() + " - " + p.getVersion() + ")");
                    }
                });
                t.start();
            }
        });

        launch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Thread t = new Thread(() -> {
                    try{
                        downloadFile.setEnabled(false);
                        launch.setEnabled(false);

                        Profile p = profiles.getSelectedValue();
                        if(p == null){
                            JOptionPane.showMessageDialog(main,"Please select a Profile first.");
                            launch.setEnabled(true);
                            downloadFile.setEnabled(true);
                            return;
                        }

                        String destination = "files/" + p.getAuthor() + "/" + p.getGame() + "/" + p.getVersion() + "-" + p.getFile();
                        File saveLocaton = new File(destination);
                        if(!saveLocaton.exists()) {
                            destination = "files/" + p.getAuthor() + "/" + p.getGame() + "/resources.zip";

                            FileDownloader.downloadFile("https://github.com/" + p.getAuthor() + "/" + p.getGame() + "/releases/download/" + p.getVersion() + "/resources.zip", destination, status);

                            long lastSpeedUpdate = System.currentTimeMillis();
                            long speed = 0;
                            long lastBytes = 0;

                            while (!status.isComplete()) {
                                if(System.currentTimeMillis() - lastSpeedUpdate > 1000){
                                    lastSpeedUpdate = System.currentTimeMillis();
                                    speed = status.getBytesRead() - lastBytes;
                                    lastBytes = status.getBytesRead();
                                }

                                String timeLeft = "Waiting...";

                                if(speed > 0){
                                    long time = ((status.getBytesTotal() - status.getBytesRead()) / speed) * 1000;
                                    timeLeft = Util.convertMillisToHMS(time);
                                }
                                progress.setMaximum((int) status.getBytesTotal());
                                progress.setValue((int) status.getBytesRead());
                                progress.setString("Downloading Main File " + String.format("%.2f",status.getBytesRead() / 1024f / 1024f) + "MB / " + String.format("%.2f",status.getBytesTotal() / 1024f / 1024f) + " MB (" + String.format("%.2f%%", ((float) status.getBytesRead() / (float) status.getBytesTotal()) * 100f) + "), " + String.format("%.2f",speed / 1024f / 1024f) + " MB/s, Time left: " + timeLeft);
                            }

                            progress.setString("Download Complete (" + p.getGame() + " - " + p.getVersion() + ")");
                            Notification.notification("ODLauncher","Completed download of " + p.getGame() + " - " + p.getVersion());
                            System.out.println("Download Complete");
                            progress.setValue(100);
                            progress.setMaximum(100);

                            status.reset();

                            /**
                             *
                             *
                             */

                            destination = "files/" + p.getAuthor() + "/" + p.getGame() + "/" + p.getVersion() + "-" + p.getFile();

                            FileDownloader.downloadFile("https://github.com/" + p.getAuthor() + "/" + p.getGame() + "/releases/download/" + p.getVersion() + "/" +p.getFile(), destination, status);

                            lastSpeedUpdate = System.currentTimeMillis();
                            speed = 0;
                            lastBytes = 0;

                            while (!status.isComplete()) {
                                if(System.currentTimeMillis() - lastSpeedUpdate > 1000){
                                    lastSpeedUpdate = System.currentTimeMillis();
                                    speed = status.getBytesRead() - lastBytes;
                                    lastBytes = status.getBytesRead();
                                }

                                String timeLeft = "Waiting...";

                                if(speed > 0){
                                    long time = ((status.getBytesTotal() - status.getBytesRead()) / speed) * 1000;
                                    timeLeft = Util.convertMillisToHMS(time);
                                }
                                progress.setMaximum((int) status.getBytesTotal());
                                progress.setValue((int) status.getBytesRead());
                                progress.setString("Downloading Main File " + String.format("%.2f",status.getBytesRead() / 1024f / 1024f) + "MB / " + String.format("%.2f",status.getBytesTotal() / 1024f / 1024f) + " MB (" + String.format("%.2f%%", ((float) status.getBytesRead() / (float) status.getBytesTotal()) * 100f) + "), " + String.format("%.2f",speed / 1024f / 1024f) + " MB/s, Time left: " + timeLeft);
                            }

                            progress.setString("Download Complete (" + p.getGame() + " - " + p.getVersion() + ")");
                            Notification.notification("ODLauncher","Completed download of " + p.getGame() + " - " + p.getVersion());
                            System.out.println("Download Complete");
                            progress.setValue(100);
                            progress.setMaximum(100);
                        } else {
                            status.setComplete(true);
                        }

                        try {
                            String startCommand = "java -jar " + new File(destination).getName() + " " + TOKEN + " false";

                            Process pr = Runtime.getRuntime().exec(startCommand,null,new File(destination).getParentFile());

                            Scanner errorScanner = new Scanner(pr.getErrorStream());
                            Scanner outputScanner = new Scanner(pr.getInputStream());

                            Thread th = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Console console = new Console(pr);

                                    console.run();

                                    console.log(Level.INFO, startCommand);
                                    console.log(Level.INFO, "Process starting");

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            while(true){
                                                try {
                                                    if (!pr.isAlive() && !(pr.getErrorStream().available() > 0 || pr.getInputStream().available() > 0)) break;
                                                } catch (IOException ex) {
                                                    throw new RuntimeException(ex);
                                                }
                                                try{
                                                    if(pr.getErrorStream().available() > 0){
                                                        String error = errorScanner.nextLine();
                                                        console.log(Level.ERR, error);
                                                    }else if(pr.getInputStream().available() > 0){
                                                        String info = outputScanner.nextLine();
                                                        console.log(Level.INFO, info);
                                                    }
                                                }catch (Exception e){
                                                    e.printStackTrace();
                                                }
                                            }
                                            console.log(Level.INFO, "");
                                            console.log(Level.INFO, "Process ended with exit code " + pr.exitValue());
                                            if(pr.exitValue() != 0){
                                                JOptionPane.showMessageDialog(main.getContentPane(),"The process terminated unexpectedly. Please check the logs for further information. \n\nExit code " + pr.exitValue(),"Error",JOptionPane.ERROR_MESSAGE);
                                            }
                                        }
                                    }).start();
                                }
                            });
                            th.start();
                            System.out.println("End");
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            //console.close();
                            launch.setEnabled(true);
                            downloadFile.setEnabled(true);
                            JOptionPane.showMessageDialog(main.getContentPane(),ex.getMessage() + "\nConsider Re-Downloading the file.","Error",JOptionPane.ERROR_MESSAGE);
                            throw new RuntimeException(ex);
                        }
                        launch.setEnabled(true);
                        downloadFile.setEnabled(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        launch.setEnabled(true);
                        downloadFile.setEnabled(true);
                        JOptionPane.showMessageDialog(main.getContentPane(),ex.getMessage() + "\nConsider Re-Downloading the file.","Error",JOptionPane.ERROR_MESSAGE);
                    }
                });
                t.start();
            }
        });

        Notifications.getInstance().setJFrame(null);
        //Notifications.getInstance().show(Notifications.Type.SUCCESS, Notifications.Location.BOTTOM_RIGHT, "Success");
        //Notifications.getInstance().show(Notifications.Location.BOTTOM_RIGHT,new JLabel("Test"));
        //Notifications.getInstance().show(new JButton("My Custom Button"));
        //Notifications.getInstance().clear(Notifications.Location.TOP_RIGHT);
        //Notifications.getInstance().clearAll();

        //Notification.notification("Test","Test Message");

    }
    public static void main(String[] args) throws Exception {
        IntelliJTheme.setup(Main.class.getResourceAsStream("/themes/theme.purple.json"));
        Notification.init();
        //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        File file = new File("odlauncher/config.json");
        File dumpFile = new File("dump.json");
        dumpFile.createNewFile();
        //TODO reactivate
        //System.setErr(new PrintStream(dumpFile));
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

    public void saveAllProfiles(){
        JSONObject profilesObject = new JSONObject();

        Enumeration<Profile> profileEnumeration = profilesModel.elements();

        while (profileEnumeration.hasMoreElements()) {
            Profile p = profileEnumeration.nextElement();

            if(profileJList.getSelectedValue() != null && profileJList.getSelectedValue().equals(p)){
                if(games.getSelectedIndex() != -1) p.setGame(games.getSelectedValue());
                if(versions.getSelectedIndex() != -1) p.setVersion(versions.getSelectedValue());
                if(files.getSelectedIndex() != -1) p.setFile(files.getSelectedValue());
                p.setName(profileName.getText());
                profileName.setText(p.getName());
            }
            profilesObject.put(p.getUuid(),p.asJSON());
        }

        config.put("profiles",profilesObject);

        try {
            CacheServer.writeStringToFile(config,configSaveFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

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
                panel.setBackground(list.getSelectionBackground());
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

            JLabel name = new JLabel(profile.getName());
            JLabel game = new JLabel(profile.getGame());
            JLabel version = new JLabel(profile.getVersion());

            if(profile.getIcon() == null){
                BufferedImage bf = new BufferedImage(60,60,1);
                Graphics2D g = bf.createGraphics();
                g.setColor(Color.WHITE);
                g.fillRect(0,0,60,60);
                g.setColor(Color.GRAY);
                g.setFont(new Font("Arial",Font.PLAIN,60));
                g.drawString("?",15,52);
                g.dispose();
                image.setIcon(new ImageIcon(bf));
            } else {
                try {
                    image.setIcon(new ImageIcon(Main.resize(ImageIO.read(new File(profile.getIcon())),60,60)));
                } catch (Exception e) {
                    BufferedImage bf = new BufferedImage(60,60,1);
                    Graphics2D g = bf.createGraphics();
                    g.setColor(Color.WHITE);
                    g.fillRect(0,0,60,60);
                    g.setColor(Color.RED);
                    g.setFont(new Font("Arial",Font.PLAIN,60));
                    g.drawString("?",15,52);
                    g.dispose();
                    image.setIcon(new ImageIcon(bf));
                }
            }

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