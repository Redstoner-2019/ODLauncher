package me.redstoner2019.client.github;

import me.redstoner2019.client.gui.Game;
import me.redstoner2019.client.gui.Version;
import me.redstoner2019.util.http.Method;
import me.redstoner2019.util.http.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

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

            return gameList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public static List<Version> getVersions(Game game){
        if(game == null) return new ArrayList<>();
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
            Collections.reverse(versionList);

            return versionList;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static Version getNewestVersion(Game game){
        if(game == null) return new Version();
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
            Collections.reverse(versionList);

            return versionList.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return new Version();
        }
    }
}
