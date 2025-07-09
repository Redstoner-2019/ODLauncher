package me.redstoner2019.client.github;

import me.redstoner2019.client.downloading.DownloadStatus;
import me.redstoner2019.client.downloading.FileDownloader;
import me.redstoner2019.client.gui.Version;

public class DownloadGame {
    public static void install(String url, Version version, DownloadStatus generalStat, DownloadStatus actionStat){
        FileDownloader.downloadRelease(url.replaceAll("tag","download"),"installations/" + version.getGame().getOwner() + "/" + version.getGame().getName() + "/" + version.getVersion(),actionStat, generalStat);
    }

    public static void uninstall(String url){
        FileDownloader.deleteRelease(url.replaceAll("tag","download"));
    }
}
