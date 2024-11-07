package me.redstoner2019;

import raven.toast.Notifications;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Notification {
    private static TrayIcon trayIcon;
    private static SystemTray tray;
    public static void init(){
        if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported!");
            return;
        }

        tray = SystemTray.getSystemTray();

        PopupMenu popupMenu = new PopupMenu();

        // Create a notification item
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        popupMenu.add(exitItem);

        trayIcon = new TrayIcon(new BufferedImage(50,50,1), "ODLauncher", popupMenu);
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static void notification(String title, String message){
        /*if (!SystemTray.isSupported()) {
            System.out.println("System tray is not supported!");
            return;
        }
        if(trayIcon == null) init();
        try {
            trayIcon.setImage(ImageIO.read(new File("C:\\Users\\Redstoner_2019\\Pictures/reved_0112.jpg")));
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        //Notifications.getInstance().show(Notifications.Type.INFO, Notifications.Location.BOTTOM_RIGHT, title + ": " + message);
        JPanel panel = new JPanel();

        JTextArea titleLabel = new JTextArea(title);
        JTextArea messageLabel = new JTextArea(message);

        titleLabel.setEditable(false);
        messageLabel.setEditable(false);

        panel.add(titleLabel);
        panel.add(messageLabel);

        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        messageLabel.setLineWrap(true);

        titleLabel.setBounds(0,0,400,titleLabel.getFont().getSize()+10);
        messageLabel.setBounds(0,titleLabel.getFont().getSize()+15,400,150-titleLabel.getFont().getSize());

        panel.setLayout(null);
        panel.setSize(400,150);

        Notifications.getInstance().show(Notifications.Location.BOTTOM_RIGHT,5000,panel);
        Toolkit.getDefaultToolkit().beep();
    }

    public static void showCustomNotification(String title, String message, String imagePath) {
        // Create a JWindow to act as the custom notification
        JWindow notificationWindow = new JWindow();
        notificationWindow.setLayout(new BorderLayout());

        // Create components
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        JLabel imageLabel = new JLabel(new ImageIcon(imagePath));

        // Add components to window
        notificationWindow.add(titleLabel, BorderLayout.NORTH);
        notificationWindow.add(imageLabel, BorderLayout.CENTER);
        notificationWindow.add(messageLabel, BorderLayout.SOUTH);

        // Set window size and position
        notificationWindow.setSize(300, 200);
        notificationWindow.setLocationRelativeTo(null);  // Center it
        notificationWindow.setVisible(true);

        // Auto-hide the window after a few seconds
        new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notificationWindow.setVisible(false);
                notificationWindow.dispose();
            }
        }).start();
    }
}
