package me.redstoner2019.client.github;

import me.redstoner2019.Utilities;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CacheRequest {
    public static List<String> getGames(){
        try {
            String cacheAddress = Utilities.getIPData().getString("auth-server");
            URL url = new URL("http://" + cacheAddress + "/api/repositories");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            JSONObject games = new JSONObject(new String(connection.getInputStream().readAllBytes()));

            List<String> gameList = new ArrayList<>();
            gameList.addAll(games.keySet());

            return gameList;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public static List<String> getVersions(String game){
        try {
            String cacheAddress = Utilities.getIPData().getString("auth-server");
            URL url = new URL("http://" + cacheAddress + "/api/" + game + "/info");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            JSONObject info = new JSONObject(new String(connection.getInputStream().readAllBytes()));

            List<String> versionList = new ArrayList<>();
            for (int i = 0; i < info.getJSONArray("versions").length(); i++) {
                versionList.add(info.getJSONArray("versions").getString(i));
            }

            return versionList;
        } catch (IOException e) {
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
