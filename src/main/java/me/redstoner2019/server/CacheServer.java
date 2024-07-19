package me.redstoner2019.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.redstoner2019.StatisticClient;
import me.redstoner2019.client.github.GitHubReleasesFetcher;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

public class CacheServer {
    public static void main(String[] args) throws IOException {
        File data = new File("cacheServer/index.json");
        if(!data.exists()) {
            data.getParentFile().mkdirs();
            data.createNewFile();
            JSONObject cacheData = new JSONObject();
            cacheData.put("repos",new JSONObject());
            writeStringToFile(cacheData,data);
        }

        Thread updater = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        JSONObject cache = loadCache();
                        for(String user : cache.getJSONObject("repos").keySet()){
                            JSONObject repos = cache.getJSONObject("repos").getJSONObject(user);
                            for(String repo : repos.keySet()){
                                JSONArray versions = repos.getJSONArray(repo);

                                List<String> versionsRequested = GitHubReleasesFetcher.fetchAllReleases(user,repo);

                                for (int i = 0; i < versions.length(); i++) {
                                    String version = versions.getString(i);
                                    versionsRequested.remove(version);
                                }

                                for(String v : versionsRequested){
                                    System.out.println("Found new Version for repo " + repo + ", " + user + ": " + v);
                                    addVersion(user,repo,v);
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        updater.start();

        ServerSocket serverSocket = new ServerSocket(8001);
        while (serverSocket.isBound()) {
            Thread t = new Thread(() -> {
                try {
                    new ClientHandler(serverSocket.accept());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }
    }

    public static JSONObject loadCache() throws IOException {
        File data = new File("cacheServer/index.json");
        return new JSONObject(readFile(data));
    }
    public static void save(JSONObject object) throws IOException {
        File data = new File("cacheServer/index.json");
        writeStringToFile(object,data);
    }

    public static void addUser(String user) throws IOException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        repos.put(user,new JSONObject());
        cacheData.put("repos",repos);
        save(cacheData);
    }
    public static void addRepo(String user, String repo) throws IOException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        if(!repos.has(user)){
            addUser(user);
            cacheData = loadCache();
        }
        JSONObject repositories = repos.getJSONObject(user);
        repositories.put(repo, new JSONArray());
        repos.put(user, repositories);
        cacheData.put("repos",repos);
        save(cacheData);
    }
    public static void addVersion(String user, String repo,String version) throws IOException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        if(!repos.has(user)){
            addUser(user);
            cacheData = loadCache();
        }
        JSONObject repositories = repos.getJSONObject(user);
        if(!repositories.has(repo)){
            addRepo(user,repo);
            cacheData = loadCache();
        }
        JSONArray versions = repositories.getJSONArray(repo);
        versions.put(version);
        repositories.put(repo, versions);
        repos.put(user, repositories);
        cacheData.put("repos",repos);
        save(cacheData);
    }

    public static String prettyJSON(String uglyJsonString) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            Object jsonObject = objectMapper.readValue(uglyJsonString, Object.class);
            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            return prettyJson;
        }catch (Exception e){
            return null;
        }
    }
    public static void writeStringToFile(JSONObject str, File file) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] strToBytes = prettyJSON(str.toString()).getBytes();
        outputStream.write(strToBytes);

        outputStream.close();
    }
    public static String readFile(File path) throws IOException {
        byte[] encoded = Files.readAllBytes(path.toPath());
        return new String(encoded, Charset.defaultCharset());
    }
}
