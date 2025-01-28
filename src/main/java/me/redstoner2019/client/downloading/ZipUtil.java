package me.redstoner2019.client.downloading;

import java.io.*;
import java.util.zip.*;

public class ZipUtil {
    public static void unzip(String zipFilePath, String destDir, DownloadStatus status) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File dir = new File(destDir);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }

                    int totalEntries = countZipEntries(zipFilePath);
                    int currentEntry = 0;

                    try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
                        ZipEntry entry = zipIn.getNextEntry();
                        while (entry != null) {
                            currentEntry++;
                            String filePath = destDir + File.separator + entry.getName();
                            if (!entry.isDirectory()) {
                                extractFile(zipIn, filePath);
                            } else {
                                File dirEntry = new File(filePath);
                                dirEntry.mkdirs();
                            }
                            status.setComplete(false);
                            status.setBytesRead(currentEntry);
                            status.setBytesTotal(totalEntries);

                            zipIn.closeEntry();
                            entry = zipIn.getNextEntry();
                        }
                    }
                    status.setComplete(true);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        t.start();

    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[4096];
            int read;
            while ((read = zipIn.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }

    private static int countZipEntries(String zipFilePath) throws IOException {
        int count = 0;
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            while (zipIn.getNextEntry() != null) {
                count++;
            }
        }
        return count;
    }
}
