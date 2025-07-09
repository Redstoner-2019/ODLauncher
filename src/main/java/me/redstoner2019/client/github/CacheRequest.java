package me.redstoner2019.client.github;

import me.redstoner2019.Main;
import me.redstoner2019.Utilities;
import me.redstoner2019.client.gui.Game;
import me.redstoner2019.client.gui.Version;
import me.redstoner2019.util.http.Method;
import me.redstoner2019.util.http.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static me.redstoner2019.client.gui.Main.TOKEN;

public class CacheRequest {

    public static List<Game> getGames(){
        try {
            JSONObject request = new JSONObject();
            request.put("token", TOKEN);

            JSONObject result = Requests.request(Method.GET, "https://stats.redstonerdev.io/stats/game/getAll", request);
            JSONArray games = new JSONArray(result.getString("body"));

            List<Game> gameList = new ArrayList<>();
            for (int i = 0; i < games.length(); i++) {
                JSONObject o = games.getJSONObject(i);
                gameList.add(new Game(o.getString("owner"),o.getLong("created"),o.getString("name"),o.getString("id")));
            }

            System.out.println("Games found " + gameList.size() + " games." );

            return gameList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static List<Version> getVersions(Game game){
        if(game == null) return new ArrayList<>();
        System.out.println("Getting versions for " + game + " from cache server...");
        try {
            JSONObject request = new JSONObject();
            request.put("token", TOKEN);
            request.put("game", game.getId());

            JSONObject result = Requests.request(Method.POST, "https://stats.redstonerdev.io/stats/versions/getAll", request);
            JSONArray versions = new JSONArray(result.getString("body"));

            List<Version> versionList = new ArrayList<>();
            for (int i = 0; i < versions.length(); i++) {
                JSONObject version = versions.getJSONObject(i);
                versionList.add(new Version(game, version.optString("releaseURL",""), version.getString("id"), version.getString("version"), version.getInt("versionNumber")));
            }

            versionList.sort(Comparator.comparingInt(Version::getVersionNumber));

            return versionList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static List<String> getFiles(String game, String version){
        try {
            String cacheAddress = Utilities.getIPData().getString("auth-server");
            URL url = new URL("http://" + cacheAddress + "/api/" + game + "/" + version);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            JSONObject info = new JSONObject(new String(connection.getInputStream().readAllBytes()));

            List<String> filelist = new ArrayList<>();
            filelist.addAll(info.keySet());

            return filelist;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}
