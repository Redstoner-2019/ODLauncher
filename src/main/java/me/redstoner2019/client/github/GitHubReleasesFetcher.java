package me.redstoner2019.client.github;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import me.redstoner2019.Utilities;
import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.server.AuthServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubReleasesFetcher {

    private static final String GITHUB_API_URL_TEMPLATE = "https://api.github.com/repos/%s/%s/releases";

    public static List<String> fetchAllReleases(String owner, String repo) throws Exception {
        List<String> releases = new ArrayList<>();
        String url = String.format(GITHUB_API_URL_TEMPLATE, owner, repo);
        while (url != null) {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

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

    public static List<String> fetchAllReleaseFiles(String owner, String repo, String version) throws Exception {
        List<String> files = new ArrayList<>();
        URL url = new URL(String.format("https://api.github.com/repos/%s/%s/releases", owner, repo));

        JSONArray data = new JSONArray(new String(url.openConnection().getInputStream().readAllBytes()));

        for (int i = 0; i < data.length(); i++) {
            JSONObject object = data.getJSONObject(i);
            if(object.getString("tag_name").equals(version)){
                JSONArray assets = object.getJSONArray("assets");
                for (int j = 0; j < assets.length(); j++) {
                    JSONObject fileStats = assets.getJSONObject(j);
                    System.out.println("-------------------------------------------------------------");
                    System.out.println("Download URL: " + fileStats.getString("browser_download_url"));
                    System.out.println("Downloads: " + fileStats.getInt("download_count"));
                    System.out.println("Updated: " + fileStats.getString("updated_at"));
                    System.out.println("File Size: " + fileStats.getInt("size"));
                    System.out.println();
                    files.add(fileStats.getString("browser_download_url"));
                }
            }
        }
        return files;
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
}

