package me.redstoner2019.client.downloading;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class FileDownloader {
    public static void downloadFile(String fileUrl, String destinationFilePath, DownloadStatus status) {
        File file = new File(destinationFilePath);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    long l = System.currentTimeMillis();

                    URL url = new URL(fileUrl);
                    URLConnection urlConnection = url.openConnection();
                    HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.connect();

                    int response = httpURLConnection.getResponseCode();

                    status.setStatus(response);
                    status.setBytesTotal(urlConnection.getHeaderFieldLong("Content-Length",1));
                    System.out.println(urlConnection.getHeaderFieldLong("Content-Length",1));

                    for(String s : urlConnection.getHeaderFields().keySet()){
                        System.out.println(String.format(" - %-50s   |   %-50s", s, urlConnection.getHeaderField(s)));
                    }

                    FileOutputStream outputStream = new FileOutputStream(destinationFilePath);

                    if (response == 200) {
                        InputStream data = urlConnection.getInputStream();

                        int packetSize = 256000;

                        byte[] dataRead = data.readNBytes(packetSize);

                        status.setBytesRead(data.available());

                        while (dataRead.length > 0) {
                            status.setBytesRead(status.getBytesRead() + dataRead.length);
                            outputStream.write(dataRead);
                            dataRead = data.readNBytes(packetSize);
                        }

                        outputStream.flush();
                        outputStream.close();
                        System.out.println("File downloaded successfully!");
                    } else {
                        System.err.println("Failed to download file: " + response);
                        System.err.println("File: " + fileUrl);
                    }

                    System.out.println("Took " + (System.currentTimeMillis() - l) + " ms");
                    status.setComplete(true);
                }catch (Exception e){

                }
            }
        });
        t.start();

    }
}
