package me.redstoner2019.client.github;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class GitHub {
    private static final String TOKEN = "";
    private static final String GITHUB_API_URL_TEMPLATE = "https://api.github.com/repos/%s/%s/releases";
    public static String authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(("Redstoner-2019:" + TOKEN).getBytes());

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
}

