package me.redstoner2019.client.downloading;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DataRequest {
    public static String request(String address) throws IOException {
        URL url = new URL(address);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        System.out.println(connection.getResponseCode());

        return new String(connection.getInputStream().readAllBytes());
    }
}
