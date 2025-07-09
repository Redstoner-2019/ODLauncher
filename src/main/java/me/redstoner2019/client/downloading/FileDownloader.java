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

    public static void deleteRelease(String releaseUrl){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url = new URL(releaseUrl + "/game.json");
                    URLConnection urlConnection = url.openConnection();
                    urlConnection.setReadTimeout(5000);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.connect();
                }catch (Exception e){

                }
            }
        });
        t.start();
    }

    public static int downloadRelease(String releaseUrl, String destinationPath, DownloadStatus status, DownloadStatus generalStatus){
        if(new File(destinationPath + "/installed").exists()) {
            generalStatus.setComplete(true);
            generalStatus.setMessage("Already installed");
            generalStatus.setBytesRead(1);
            generalStatus.setBytesTotal(1);

            status.setMessage("Already installed");
            status.setBytesRead(1);
            status.setBytesTotal(1);
            return 0;
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                generalStatus.setComplete(false);
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
                    //String versionName = scanner.nextLine();
                    String versionName = "linux-x64";

                    JSONObject versionObject = jsonObject.getJSONObject("versions").optJSONObject(versionName);

                    JSONObject main = versionObject.getJSONObject("main");
                    JSONObject files = versionObject.getJSONObject("files");

                    File saveFile = new File(destinationPath + main.getString("saveLocation").replaceAll("%version%",versionName) + main.getString("name"));
                    if(!saveFile.exists()){
                        saveFile.getParentFile().mkdirs();
                        saveFile.createNewFile();
                    }

                    String startCmd = versionObject.getString("launch");
                    startCmd = startCmd.replace("%filename%",main.getString("saveLocation").replaceAll("%version%",versionName).substring(1) + main.getString("name"));

                    JSONObject launch = new JSONObject();
                    launch.put("launch",startCmd);
                    new File(destinationPath).mkdirs();
                    FileOutputStream fos = new FileOutputStream(destinationPath + "/launch.json");
                    fos.write(launch.toString(3).getBytes());
                    fos.close();

                    int steps = 1;
                    for(String fileName : files.keySet()){
                        JSONObject file = files.getJSONObject(fileName);
                        steps+=(1 + file.getJSONObject("actions").keySet().size());
                        System.out.println(fileName + " " + file.getJSONObject("actions").keySet().size());
                    }

                    generalStatus.setBytesTotal(steps);
                    generalStatus.setBytesRead(0);
                    System.out.println(steps);

                    status.setMessage("Downloading " + releaseUrl + "/" + main.getString("name") + " to " + destinationPath + main.getString("saveLocation"));
                    downloadFile(releaseUrl + "/" + main.getString("name"), saveFile.toString(), status);

                    while (!status.isComplete()) {
                        Thread.sleep(10);
                    }

                    generalStatus.setBytesRead(1);

                    for(String fileName : files.keySet()){
                        JSONObject file = files.getJSONObject(fileName);
                        System.out.println(fileName);

                        saveFile = new File(destinationPath + file.getString("location").replaceAll("%version%",version) + fileName);
                        if(!saveFile.exists()){
                            saveFile.getParentFile().mkdirs();
                            saveFile.createNewFile();
                        }

                        status.reset();

                        status.setMessage("Downloading " + fileName + " to " + saveFile);
                        downloadFile(releaseUrl + "/" + fileName, saveFile.toString(), status);

                        while (!status.isComplete()) {
                            Thread.sleep(10);
                        }

                        generalStatus.setBytesRead(generalStatus.getBytesRead() + 1);

                        for (String action : file.getJSONObject("actions").keySet()) {
                            switch (action){
                                case "unzip"-> {
                                    JSONObject actionObject = file.getJSONObject("actions").optJSONObject(action);
                                    status.reset();
                                    ZipUtil.unzip(destinationPath + file.getString("location") + fileName, destinationPath + actionObject.getString("location"), status);
                                    System.out.println("Unzipping " + fileName);
                                    while (!status.isComplete()) {
                                        if(status.getBytesTotal() == 0){
                                            status.setMessage("Starting Unzipping " + destinationPath + file.getString("location") + fileName + " to " + destinationPath + actionObject.getString("location") + "...");
                                        } else {
                                            status.setMessage("Unzipping " + destinationPath + file.getString("location") + fileName + " to " + destinationPath + actionObject.getString("location"));
                                        }
                                        Thread.sleep(10);
                                    }
                                    if(actionObject.getBoolean("deleteAfter")){
                                        new File(destinationPath + file.getString("location") + fileName).delete();
                                        status.setMessage("Deleting " + destinationPath + file.getString("location") + fileName);
                                    }
                                }
                            }
                            generalStatus.setBytesRead(generalStatus.getBytesRead() + 1);
                        }
                        System.out.println(startCmd);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                generalStatus.setComplete(true);

                try {
                    System.out.println(destinationPath + "/installed");
                    new File(destinationPath).mkdirs();
                    new File(destinationPath + "/installed").createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
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

                        byte[] dataRead = data.readNBytes(packetSize);

                        while (dataRead.length > 0) {
                            status.setBytesRead(status.getBytesRead() + dataRead.length);
                            outputStream.write(dataRead);
                            dataRead = data.readNBytes(packetSize);
                        }

                        outputStream.flush();
                        outputStream.close();
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
