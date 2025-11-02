package me.redstoner2019.client.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SelectFile extends JDialog {
    private JList<String> itemList;
    private DefaultListModel<String> listModel;
    private String selected;
    private String link;

    private void showPopup() {
        itemList.clearSelection();
        setVisible(true);
    }

    public String getSelected() {
        return selected;
    }

    public String getLink() {
        return link;
    }
}

