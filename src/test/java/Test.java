import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;

public class Test {
    public static void main(String[] args) {
        FileDownloader.downloadRelease("https://github.com/Redstoner-2019/FNaF/releases/tag/v1.4.1-alpha.1".replaceAll("tag","download"),"gameTest/Redstoner-2019/FNaF",new DownloadStatus());
    }
}