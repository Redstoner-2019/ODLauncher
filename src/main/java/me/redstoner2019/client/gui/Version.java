package me.redstoner2019.client.gui;

import org.json.JSONObject;

public class Version {
    private Game game;
    private String releaseURL;
    private String id;
    private String version;
    private int versionNumber;

    public Version(Game game, String releaseURL, String id, String version, int versionNumber) {
        this.game = game;
        this.releaseURL = releaseURL;
        this.id = id;
        this.version = version;
        this.versionNumber = versionNumber;
    }

    public Version() {
        this.game = new Game();
        this.releaseURL = "";
        this.id = "";
        this.version = "";
        this.versionNumber = 0;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public String getReleaseURL() {
        return releaseURL;
    }

    public void setReleaseURL(String releaseURL) {
        this.releaseURL = releaseURL;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("game",game.toJSON());
        o.put("releaseURL",releaseURL);
        o.put("id",id);
        o.put("version",version);
        o.put("versionNumber",versionNumber);
        return o;
    }

    public static Version fromJSON(JSONObject o){
        Version version = new Version();
        if(o.has("game")) version.setGame(Game.fromJSON(o.getJSONObject("game"))); else version.game = new Game("",0,"","");
        if(o.has("releaseURL")) version.setReleaseURL(o.getString("releaseURL")); else version.releaseURL = "";
        if(o.has("id")) version.setId(o.getString("id")); else version.id = "";
        if(o.has("version")) version.setVersion(o.getString("version")); else version.version = "";
        if(o.has("versionNumber")) version.setVersionNumber(o.getInt("versionNumber")); else version.versionNumber = 0;
        return version;
    }

    @Override
    public String toString() {
        return version;
    }
}
