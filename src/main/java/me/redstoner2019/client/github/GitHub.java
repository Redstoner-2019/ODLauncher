package me.redstoner2019.client.github;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

public class GitHub {
    private static final String TOKEN = "";
    private static final String GITHUB_API_URL_TEMPLATE = "https://api.github.com/repos/%s/%s/releases";
    public static String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(("Redstoner-2019:" + TOKEN).getBytes());
    public static HashMap<String, Attatchment> cachedFiles = new HashMap<>();

    public static JSONArray getAttatchments(String releaseUrl) throws Exception {
        if(cachedFiles.containsKey(releaseUrl) && LocalDateTime.now().isBefore(cachedFiles.get(releaseUrl).getFetch())){
            return cachedFiles.get(releaseUrl).getData();
        }

        //cachedFiles.put(releaseUrl, new Attatchment(new JSONArray()));

        //if(true){
        //    return new JSONArray();
        //}

        String apiUrl = convertToApiUrl(releaseUrl);
        if (apiUrl == null) {
            System.err.println("Invalid GitHub release URL");
            return new JSONArray();
        }

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.err.println("Failed to fetch release info: " + response.statusCode());
            return new JSONArray();
        }

        JSONObject json = new JSONObject(response.body());
        JSONArray assets = json.getJSONArray("assets");

        cachedFiles.put(releaseUrl, new Attatchment(assets));

        return assets;
    }

    public static String convertToApiUrl(String releaseUrl) {
        Pattern pattern = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)/releases/tag/(.+)");
        Matcher matcher = pattern.matcher(releaseUrl);
        if (matcher.matches()) {
            String owner = matcher.group(1);
            String repo = matcher.group(2);
            String tag = matcher.group(3);
            return String.format("https://api.github.com/repos/%s/%s/releases/tags/%s", owner, repo, tag);
        }
        return null;
    }

    public static List<String> fetchAllReleases(String owner, String repo) throws Exception {
        List<String> releases = new ArrayList<>();
        String url = String.format(GITHUB_API_URL_TEMPLATE, owner, repo);
        while (url != null) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            connection.addRequestProperty("Accept", "application/vnd.github.v3+json");

            connection.setRequestProperty("Authorization", authHeaderValue);

            if(connection.getResponseCode() == 403){
                System.err.println("[403], API limit exceeded");
                return new ArrayList<>();
            }
            if(connection.getResponseCode() == 401){
                System.err.println("[401], Unauthorized");
                return new ArrayList<>();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            JSONArray jsonArray = new JSONArray(content.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject release = jsonArray.getJSONObject(i);
                releases.add(release.getString("tag_name"));
            }

            url = getNextPageUrl(connection);
        }
        return releases;
    }

    public static List<JSONObject> fetchAllReleaseFiles(String owner, String repo, String version) throws Exception {
        List<JSONObject> fileData = new ArrayList<>();
        URL url = new URL(String.format("https://api.github.com/repos/%s/%s/releases", owner, repo));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.addRequestProperty("Accept", "application/vnd.github.v3+json");

        connection.setRequestProperty("Authorization", authHeaderValue);

        if(connection.getResponseCode() == 403){
            System.err.println("403, API limit exceeded");
            return new ArrayList<>();
        }

        JSONArray data = new JSONArray(new String(connection.getInputStream().readAllBytes()));

        for (int i = 0; i < data.length(); i++) {
            JSONObject object = data.getJSONObject(i);
            if(object.getString("tag_name").equals(version)){
                JSONArray assets = object.getJSONArray("assets");
                for (int j = 0; j < assets.length(); j++) {
                    JSONObject fileStats = assets.getJSONObject(j);

                    JSONObject reducedAsset = new JSONObject();
                    reducedAsset.put("name",fileStats.getString("name"));
                    reducedAsset.put("browser_download_url",fileStats.getString("browser_download_url"));
                    reducedAsset.put("download_count",fileStats.getInt("download_count"));
                    reducedAsset.put("updated_at",fileStats.getString("updated_at"));
                    reducedAsset.put("created_at",fileStats.getString("created_at"));
                    reducedAsset.put("size",fileStats.getInt("size"));
                    fileData.add(reducedAsset);
                }
            }
        }
        return fileData;
    }

    private static String getNextPageUrl(HttpURLConnection connection) {
        String linkHeader = connection.getHeaderField("Link");
        if (linkHeader == null) {
            return null;
        }

        String[] links = linkHeader.split(", ");
        for (String link : links) {
            String[] parts = link.split("; ");
            if (parts.length < 2) {
                continue;
            }
            if ("rel=\"next\"".equals(parts[1])) {
                return parts[0].substring(1, parts[0].length() - 1);  // Remove < and >
            }
        }
        return null;
    }

    public static String fetchReadmeContent(String owner, String repo) throws Exception {
        String url = "https://raw.githubusercontent.com/" + owner + "/" + repo + "/main/README.md";
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("Accept", "application/vnd.github.v3+json");

        connection.setRequestProperty("Authorization", authHeaderValue);

        int responseCode = connection.getResponseCode();
        if (responseCode == 200) {
            return new String(connection.getInputStream().readAllBytes());
        } else {
            return "# No README.md found in " + repo + " by " + owner + ".";
        }
    }

    public static class Attatchment {
        public LocalDateTime fetch = LocalDateTime.now().plusMinutes(5);
        public JSONArray data = new JSONArray();

        public Attatchment(JSONArray data) {
            this.data = data;
        }

        public LocalDateTime getFetch() {
            return fetch;
        }

        public void setFetch(LocalDateTime fetch) {
            this.fetch = fetch;
        }

        public JSONArray getData() {
            return data;
        }

        public void setData(JSONArray data) {
            this.data = data;
        }
    }
}

