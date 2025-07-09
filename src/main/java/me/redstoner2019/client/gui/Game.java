package me.redstoner2019.client.gui;

import org.json.JSONObject;

public class Game {
    private String owner;
    private long created;
    private String name;
    private String id;

    public Game() {
        this.owner = "";
        this.created = 0;
        this.name = "";
        this.id = "";
    }

    public Game(String owner, long created, String name, String id) {
        this.owner = owner;
        this.created = created;
        this.name = name;
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public JSONObject toJSON(){
        JSONObject o = new JSONObject();
        o.put("owner",owner);
        o.put("created",created);
        o.put("name",name);
        o.put("id",id);
        return o;
    }

    public static Game fromJSON(JSONObject o){
        Game game = new Game("",0,"","");
        if(o.has("owner")) game.owner = o.getString("owner"); else game.owner = "";
        if(o.has("created")) game.created = o.getLong("created"); else game.created = 0;
        if(o.has("name")) game.name = o.getString("name"); else game.name = "";
        if(o.has("id")) game.id = o.getString("id"); else game.id = "";
        return game;
    }

    @Override
    public String toString() {
        return name;
    }
}
