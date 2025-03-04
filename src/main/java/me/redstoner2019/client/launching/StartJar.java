package me.redstoner2019.client.launching;

import me.redstoner2019.client.downloading.DownloadStatus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static me.redstoner2019.client.downloading.FileDownloader.downloadFile;

public class StartJar {
    private static final String JAVA_DOWNLOAD_URL = "https://api.adoptium.net/v3/assets/latest/%version%/hotspot?os=windows&arch=x64";
    private static final String JAVA_DIR = "runtime/java";

    public static Process startJar(String file, String JAVA_VERSION, DownloadStatus status){
        if (!isJavaVersionSufficient(JAVA_VERSION)) {
            System.out.println("Downloading Java...");
            if (!downloadAndExtractJava(status, JAVA_VERSION)) {
                System.err.println("Failed to download Java.");
                return null;
            }
        }
        return launchJar(file, JAVA_VERSION);
    }

    private static boolean isJavaVersionSufficient(String JAVA_VERSION) {
        String version = System.getProperty("java.version");
        System.out.println("Detected Java Version: " + version);
        if (version == null) return false;

        String majorVersion = version.split("\\.")[0];
        if (majorVersion.equals("1")) {  // Handle old versions like "1.8"
            majorVersion = version.split("\\.")[1];
        }
        return Integer.parseInt(majorVersion) >= Integer.parseInt(JAVA_VERSION);
    }

    private static boolean downloadAndExtractJava(DownloadStatus downloadStatus, String JAVA_VERSION) {
        try {
            Path javaZipPath = Paths.get("java_runtime.zip");

            URL url = new URL(JAVA_DOWNLOAD_URL.replaceAll("%version%",JAVA_VERSION));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            JSONArray json = new JSONArray(new String(connection.getInputStream().readAllBytes()));

            String jreURL = "";

            for (int i = 0; i < json.length(); i++) {
                JSONObject jsonObject = json.getJSONObject(i);

                String arch = jsonObject.getJSONObject("binary").getString("architecture");
                String image = jsonObject.getJSONObject("binary").getString("image_type");
                String os = jsonObject.getJSONObject("binary").getString("os");

                if(arch.equals("x64") && image.equals("jre") && os.equals("windows")) {
                    jreURL = jsonObject.getJSONObject("binary").getJSONObject("package").getString("link");
                    System.out.println(jreURL);
                    break;
                }
            }

            if(jreURL.isEmpty()) return false;


            downloadFile(jreURL, javaZipPath.toString(),downloadStatus);
            unzip(javaZipPath.toString(), JAVA_DIR);
            Files.deleteIfExists(javaZipPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void unzip(String zipFilePath, String destDirectory) throws IOException {
        Path destPath = Paths.get(destDirectory);
        Files.createDirectories(destPath);

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                Path filePath = destPath.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    Files.createDirectories(filePath);
                }
                zipIn.closeEntry();
            }
        }
    }

    private static Process launchJar(String filename, String JAVA_VERSION) {
        try {
            String javaExec = "";

            boolean sufficient = isJavaVersionSufficient(JAVA_VERSION);

            if(!sufficient) {
                for(Path p : Files.list(Path.of("runtime/java")).toList()){
                    System.out.println(p);
                    if(p.toString().contains(JAVA_VERSION)) {
                        javaExec = p + "/bin/java";
                        break;
                    }
                }

                if(javaExec.isEmpty()) return null;

                if (System.getProperty("os.name").toLowerCase().contains("win")) {
                    javaExec += ".exe";
                }
            } else {
                javaExec = "java";
            }

            ProcessBuilder pb = new ProcessBuilder(javaExec, "-jar", filename);
            pb.inheritIO();
            Process process = pb.start();
            return process;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
