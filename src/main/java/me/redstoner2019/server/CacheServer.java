package me.redstoner2019.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.redstoner2019.Util;
import me.redstoner2019.client.github.GitHub;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import static me.redstoner2019.client.github.GitHub.authHeaderValue;

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
                    long updateStart = System.currentTimeMillis();
                    System.out.println();
                    System.out.println("Pre Refresh");
                    Util.memoryInfoDump();
                    refresh();
                    System.gc();
                    System.out.println();
                    System.out.println("Post Refresh");
                    Util.memoryInfoDump();
                    try {
                        Thread.sleep((60000) - (System.currentTimeMillis() - updateStart));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        updater.start();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    System.out.println(Thread.getAllStackTraces().keySet().size() + " threads running");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        thread.start();

        ServerSocket serverSocket = new ServerSocket(8001);
        while (serverSocket.isBound()) {
            new ClientHandler(serverSocket.accept());
        }
    }

    public static void refresh(){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com/rate_limit").openConnection();
            connection.addRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", authHeaderValue);

            System.out.println(new String(connection.getInputStream().readAllBytes()));

            JSONObject cache = loadCache();
            for(String author : cache.getJSONObject("repos").keySet()){
                JSONObject repos = cache.getJSONObject("repos").getJSONObject(author);
                for(String repo : repos.keySet()){
                    JSONObject repository = repos.getJSONObject(repo);
                    List<String> versionsAvailable = GitHub.fetchAllReleases(author,repo);

                    //Check if any version is missing

                    for(String v : versionsAvailable){
                        if(!repository.has(v)){
                            addVersion(author,repo,v);
                        }
                    }

                    for(String v : versionsAvailable){
                        List<JSONObject> assets = GitHub.fetchAllReleaseFiles(author,repo,v);

                        for(JSONObject asset : assets){
                            addAsset(author,repo,v,asset);
                        }
                    }

                    cache = loadCache();
                    repos = cache.getJSONObject("repos").getJSONObject(author);

                    /*JSONObject versions = repos.getJSONObject(repo);
                    List<String> versionsRequested = GitHubReleasesFetcher.fetchAllReleases(user,repo);
                    for(String version : versionsRequested){
                        if(!versions.has(version)){
                            addVersion(user,repo,version);
                            versions = repos.getJSONObject(repo);
                            System.out.println(versions);
                            List<JSONObject> assets = GitHubReleasesFetcher.fetchAllReleaseFiles(user,repo,version);
                            for(JSONObject asset : assets){
                                String assetName = asset.getString("name");
                                JSONObject versionObject = versions.getJSONObject(version);
                                if(!versionObject.has(assetName)){
                                    System.out.println("New Asset for " +version + " " + assetName);
                                    addAsset(user,repo,version,asset);
                                }
                                System.out.println(asset);
                            }
                        }
                    }
*/
                }
                connection = (HttpURLConnection) new URL("https://api.github.com/rate_limit").openConnection();
                connection.addRequestProperty("Accept", "application/vnd.github.v3+json");
                connection.setRequestProperty("Authorization", authHeaderValue);

                System.out.println(new String(connection.getInputStream().readAllBytes()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getLocalizedMessage());
        }
    }

    public static JSONObject loadCache() throws IOException {
        File data = new File("cacheServer/index.json");
        return new JSONObject(readFile(data));
    }
    public static void save(JSONObject object) throws IOException, InterruptedException {
        File data = new File("cacheServer/index.json");
        writeStringToFile(object,data);
    }

    public static void addUser(String user) throws IOException, InterruptedException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        repos.put(user,new JSONObject());
        cacheData.put("repos",repos);
        save(cacheData);
    }
    public static void addRepo(String user, String repo) throws IOException, InterruptedException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        if(!repos.has(user)){
            addUser(user);
            cacheData = loadCache();
        }
        repos = cacheData.getJSONObject("repos");
        JSONObject repositories = repos.getJSONObject(user);
        repositories.put(repo, new JSONObject());
        repos.put(user, repositories);
        cacheData.put("repos",repos);
        save(cacheData);
    }
    public static void addVersion(String user, String repo,String version) throws IOException, InterruptedException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        if(!repos.has(user)){
            addUser(user);
            cacheData = loadCache();
        }
        repos = cacheData.getJSONObject("repos");
        JSONObject repositories = repos.getJSONObject(user);
        if(!repositories.has(repo)){
            addRepo(user,repo);
            cacheData = loadCache();
        }
        repositories = repos.getJSONObject(user);
        JSONObject versions = repositories.getJSONObject(repo);
        versions.put(version,new JSONObject());
        repositories.put(repo, versions);
        repos.put(user, repositories);
        cacheData.put("repos",repos);
        save(cacheData);
    }
    public static void addAsset(String user, String repo,String version, JSONObject asset) throws IOException, InterruptedException {
        JSONObject cacheData = loadCache();
        JSONObject repos = cacheData.getJSONObject("repos");
        if(!repos.has(user)){
            addUser(user);
            cacheData = loadCache();
        }
        repos = cacheData.getJSONObject("repos");
        JSONObject repositories = repos.getJSONObject(user);
        if(!repositories.has(repo)){
            addRepo(user,repo);
            cacheData = loadCache();
        }
        repositories = repos.getJSONObject(user);
        JSONObject versions = repositories.getJSONObject(repo);
        if(!versions.has(version)){
            addVersion(user, repo, version);
            cacheData = loadCache();
        }
        versions = repositories.getJSONObject(repo);

        JSONObject assets = versions.getJSONObject(version);
        assets.put(asset.getString("name"),asset);
        versions.put(version,assets);

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
