import me.redstoner2019.Notification;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class Test {
    public static void main(String[] args) {
        Notification.showCustomNotification("Test Title","Test Message","C:\\Users\\Redstoner_2019\\Pictures/reved_0112.jpg");
        /*JSONObject utilData = Utilities.getIPData();
        System.out.println(utilData);
        JSONObject repos = utilData.getJSONObject("repos");
        JSONObject ips = utilData.getJSONObject("ips");

        System.out.println(ips);

        for(String s : repos.keySet()){
            JSONArray arr = repos.getJSONArray(s);
            for (int i = 0; i < arr.length(); i++) {
                System.out.println("----#" + s + "#----");
                try {
                    List<String> releases = GitHubReleasesFetcher.fetchAllReleases(s, arr.getString(i));
                    releases.forEach(System.out::println);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }*/
        //System.out.println(CacheServer.prettyJSON(getFileInfo("Redstoner-2019","FNaF","v1.3.0-alpha.1","FiveNightsAtFreddys-windows-x64.jar").toString()));
    }

    public static JSONObject getFileInfo(String owner, String repo, String version, String filename) throws Exception {
        System.out.println(String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo,version));
        URL url = new URL(String.format("https://github.com/%s/%s/releases/tag/%s", owner, repo,version));

        String dataString = new String(url.openConnection().getInputStream().readAllBytes());

        System.out.println(dataString);

        JSONArray data = new JSONArray(dataString);

        System.out.println(data);

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
}
