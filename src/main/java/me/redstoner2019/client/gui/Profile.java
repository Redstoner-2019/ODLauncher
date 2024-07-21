package me.redstoner2019.client.gui;

import java.awt.image.BufferedImage;

public class Profile {
    private BufferedImage image;
    private String game;
    private String version;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile(BufferedImage image, String game, String version, String name) {
        this.image = image;
        this.game = game;
        this.version = version;
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
