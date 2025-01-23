package me.redstoner2019.client.downloading;

import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class FileDownloader {

    public static JSONObject getFileInfo(String owner, String repo, String version, String filename) throws Exception {
        URL url = new URL(String.format("https://api.github.com/repos/%s/%s/releases", owner, repo));

        JSONArray data = new JSONArray(new String(url.openConnection().getInputStream().readAllBytes()));

        for (int i = 0; i < data.length(); i++) {
            JSONObject object = data.getJSONObject(i);
            if(object.getString("tag_name").equals(version)){
                JSONArray assets = object.getJSONArray("assets");
                for (int j = 0; j < assets.length(); j++) {
                    JSONObject fileStats = assets.getJSONObject(j);

                    if(fileStats.getString("browser_download_url").endsWith(filename)){
                        JSONObject result = new JSONObject();
                        result.put("filename",fileStats.getString("name"));
                        result.put("url",fileStats.getString("browser_download_url"));
                        result.put("size",fileStats.getInt("size"));
                        result.put("downloads",fileStats.getInt("download_count"));
                        return result;
                    }
                }
            }
        }
        return new JSONObject();
    }

    public static String getFileInfoNew(String owner, String repo, String version, String filename) throws Exception {
        System.out.println(String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo,version));
        URL url = new URL(String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo,version));

        String dataString = new String(url.openConnection().getInputStream().readAllBytes());

        File file = new File("doc.html");
        if(!file.exists()){
            file.createNewFile();
        }

        new FileOutputStream(file).write(dataString.getBytes());

        return dataString;
    }

    public static boolean fileExists(String fileUrl){
        try{
            URL url = new URL(fileUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(5000);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.connect();
            return httpURLConnection.getResponseCode() == 200;
        }catch (Exception e){
            return false;
        }
    }

    public static void downloadFile(String fileUrl, String destinationFilePath, DownloadStatus status) {
        status.reset();
        File file = new File(destinationFilePath);
        if(file.exists()) file.delete();
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(fileUrl);
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setReadTimeout(5000);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.connect();

                    int response = httpURLConnection.getResponseCode();

                    status.setStatus(response);
                    status.setBytesTotal(urlConnection.getHeaderFieldLong("Content-Length",1));

                    FileOutputStream outputStream = new FileOutputStream(destinationFilePath);

                    if (response == 200) {
                        InputStream data = urlConnection.getInputStream();

                        int packetSize = 1024*32;

                        System.out.println("Packet: " + packetSize);

                        byte[] dataRead = data.readNBytes(packetSize);

                        while (dataRead.length > 0) {
                            status.setBytesRead(status.getBytesRead() + dataRead.length);
                            outputStream.write(dataRead);
                            dataRead = data.readNBytes(packetSize);
                        }

                        outputStream.flush();
                        outputStream.close();
                        System.out.println("File downloaded successfully!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to download file: " + response, "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Failed to download file: " + response);
                        System.err.println("File: " + fileUrl);
                    }

                    Thread.sleep(100);
                    status.setComplete(true);
                }catch (Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,"Download failed: " + e + "\nPlease check your Internet connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    status.setComplete(true);
                }
            }
        });
        t.start();

    }
}
