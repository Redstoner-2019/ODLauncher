package me.redstoner2019.client.gui;

import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class Profile {
    private String icon;
    private String game;
    private String version;
    private String name;
    private String author = "Redstoner-2019";
    private String file = "";
    private String uuid = "";

    public Profile() {
        uuid = UUID.randomUUID().toString();
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Profile(String image, String game, String version, String name) {
        this.icon = image;
        this.game = game;
        this.version = version;
        this.name = name;
        uuid = UUID.randomUUID().toString();
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String image) {
        this.icon = image;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public JSONObject asJSON(){
        JSONObject o = new JSONObject();
        o.put("game",game);
        o.put("version",version);
        o.put("name",name);
        o.put("author",author);
        o.put("file",file);
        o.put("uuid",uuid);
        o.put("icon",icon);
        return o;
    }
    public Profile fromJSON(JSONObject o){
        if(o.has("game")) game = o.getString("game"); else game = "";
        if(o.has("version")) version = o.getString("version"); else version = "";
        if(o.has("name")) name = o.getString("name"); else name = "";
        if(o.has("author")) author = o.getString("author"); else author = "";
        if(o.has("file")) file = o.getString("file"); else file = "";
        if(o.has("uuid")) uuid = o.getString("uuid");
        if(o.has("icon")) icon = o.getString("icon");
        else uuid = UUID.randomUUID().toString();
        return this;
    }
}
