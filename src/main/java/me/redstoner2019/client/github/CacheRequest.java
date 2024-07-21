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
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
