import me.redstoner2019.Utilities;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.github.GitHubReleasesFetcher;
import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws Exception {
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
        System.out.println(CacheServer.prettyJSON(FileDownloader.getFileInfo("Redstoner-2019","FNaF","v1.3.0-alpha","FiveNightsAtFreddys-windows-x64.jar").toString()));
    }
}
