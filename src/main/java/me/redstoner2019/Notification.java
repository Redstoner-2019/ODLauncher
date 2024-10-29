package me.redstoner2019;

import javax.imageio.ImageIO;
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

        try {
            trayIcon = new TrayIcon(ImageIO.read(new File("C:\\Users\\l.paepke\\Downloads\\images.jpg")), "ODLauncher", popupMenu);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        trayIcon.setImageAutoSize(true);
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public static void notification(String title, String message){
        trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
    }
}
