package me.redstoner2019.client.downloading;

import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    public static int downloadRelease(String releaseUrl, String destinationPath, DownloadStatus status){
        String version = "v1.4.0-alpha.1";
        try{
            URL url = new URL(releaseUrl + "/game.json");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setReadTimeout(5000);
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(5000);
            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();

            String data = new String(httpURLConnection.getInputStream().readAllBytes());
            //data = new String(new FileInputStream("C:\\Users\\l.paepke\\Downloads\\game.json").readAllBytes());

            JSONObject jsonObject = new JSONObject(data);

            if(new File(destinationPath).exists()) deleteFolder(new File(destinationPath).toPath());

            System.out.println("Versions:");
            int index = 0;
            for(String s : jsonObject.getJSONObject("versions").keySet()){
                System.out.println(index + ". " + s);
                index++;
            }
            System.out.println("Enter version:");
            Scanner scanner = new Scanner(System.in);
            String versionName = scanner.nextLine();

            JSONObject versionObject = jsonObject.getJSONObject("versions").optJSONObject(versionName);

            JSONObject main = versionObject.getJSONObject("main");
            JSONObject files = versionObject.getJSONObject("files");

            File saveFile = new File(destinationPath + main.getString("saveLocation").replaceAll("%version%",versionName) + main.getString("name"));
            if(!saveFile.exists()){
                saveFile.getParentFile().mkdirs();
                saveFile.createNewFile();
            }

            String startCmd = versionObject.getString("launch");
            startCmd = startCmd.replace("%filename%",saveFile.toString());

            System.out.println(saveFile.getAbsolutePath());
            System.out.println("Downloading " + releaseUrl + "/" + main.getString("name") + " to " + destinationPath + main.getString("saveLocation"));
            downloadFile(releaseUrl + "/" + main.getString("name"), saveFile.toString(), status);

            while (!status.isComplete()) {
                double percent = ((double) status.getBytesRead() / status.getBytesTotal()) * 100;
                System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%%", percent));
                Thread.sleep(100);
            }
            System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%% Done!", 100.0));

            for(String fileName : files.keySet()){
                JSONObject file = files.getJSONObject(fileName);

                saveFile = new File(destinationPath + file.getString("location").replaceAll("%version%",version) + fileName);
                if(!saveFile.exists()){
                    saveFile.getParentFile().mkdirs();
                    saveFile.createNewFile();
                }

                status.reset();

                System.out.println("Downloading " + fileName + " to " + saveFile);
                downloadFile(releaseUrl + "/" + fileName, saveFile.toString(), status);

                while (!status.isComplete()) {
                    double percent = ((double) status.getBytesRead() / status.getBytesTotal()) * 100;
                    System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%%", percent));
                    Thread.sleep(100);
                }
                System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%% Done!", 100.0));

                for (String action : file.getJSONObject("actions").keySet()) {
                    switch (action){
                        case "unzip"-> {
                            JSONObject actionObject = file.getJSONObject("actions").optJSONObject(action);
                            System.out.println("Unzipping " + destinationPath + file.getString("location") + fileName + " to " + destinationPath + actionObject.getString("location"));

                            status.reset();
                            ZipUtil.unzip(destinationPath + file.getString("location") + fileName, destinationPath + actionObject.getString("location"), status);
                            while (!status.isComplete()) {
                                double percent = ((double) status.getBytesRead() / status.getBytesTotal()) * 100;
                                System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%%", percent));
                                Thread.sleep(100);
                            }
                            System.out.println(status.getBytesRead() + " / " + status.getBytesTotal() + String.format(" - %.2f%% Done!", 100.0));

                            if(actionObject.getBoolean("deleteAfter")){
                                new File(destinationPath + file.getString("location") + fileName).delete();
                                System.out.println("Deleting " + destinationPath + file.getString("location") + fileName);
                            }
                        }
                    }
                }

                System.out.println(startCmd);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
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

    public static void deleteFolder(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file); // Delete each file
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir); // Delete the directory after its contents are deleted
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
