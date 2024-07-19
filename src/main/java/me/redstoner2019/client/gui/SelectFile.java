package me.redstoner2019.client.gui;

import me.redstoner2019.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SelectFile extends JDialog {
    private JList<String> itemList;
    private DefaultListModel<String> listModel;
    private String selected;
    private String link;

    public SelectFile(JFrame frame, String[] items, String[] raw) {
        super(frame, "Select a Version (" + Utilities.getPlatform() + ")", true);
        setSize(300, 500);
        setLocationRelativeTo(this);
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        for (String item : items) {
            listModel.addElement(item);
        }

        itemList = new JList<>(listModel);
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selected = itemList.getSelectedValue();
                link = raw[itemList.getSelectedIndex()];
                dispose();
            }
        });

        add(new JScrollPane(itemList), BorderLayout.CENTER);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

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

