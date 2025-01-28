import com.formdev.flatlaf.IntelliJTheme;
import me.redstoner2019.Notification;
import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.gui.Main;
import me.redstoner2019.server.CacheServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

public class Test {
    public static void main(String[] args) {
        FileDownloader.downloadRelease("https://github.com/Redstoner-2019/FNaF/releases/tag/v1.4.1-alpha.1".replaceAll("tag","download"),"gameTest/",new DownloadStatus());
    }
}