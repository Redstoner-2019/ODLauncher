package me.redstoner2019.client.gui;

import org.json.JSONObject;

import java.awt.image.BufferedImage;
import java.util.UUID;

public class Profile {
    private String icon;
    private Version version = new Version();
    private String name;
    private String author = "Redstoner-2019";
    private String file = "";
    private String uuid = "";
    private String releaseURL = "";

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

    public String getReleaseURL() {
        return releaseURL;
    }

    public Profile(String image, Version version, String name, String releaseURL) {
        this.icon = image;
        this.releaseURL = releaseURL;
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

    public Game getGame() {
        return version.getGame();
    }

    public void setGame(Game game) {
        this.version.setGame(game);
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
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
        o.put("version",version.toJSON());
        o.put("name",name);
        o.put("author",author);
        o.put("file",file);
        o.put("uuid",uuid);
        o.put("icon",icon);
        o.put("releaseURL",releaseURL);
        return o;
    }
    public Profile fromJSON(JSONObject o){
        if(o.has("version")) this.version = Version.fromJSON(o.getJSONObject("version")); else version = new Version();
        if(o.has("name")) name = o.getString("name"); else name = "";
        if(o.has("author")) author = o.getString("author"); else author = "";
        if(o.has("file")) file = o.getString("file"); else file = "";
        if(o.has("uuid")) uuid = o.getString("uuid");
        if(o.has("icon")) icon = o.getString("icon");
        if(o.has("releaseURL")) releaseURL = o.getString("releaseURL");
        else uuid = UUID.randomUUID().toString();
        return this;
    }
}
