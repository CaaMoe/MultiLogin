package moe.caa.multilogin.loader.library;

import moe.caa.multilogin.api.logger.LoggerProvider;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

public class LibraryDownloadingTask {
    private final Library library;
    private final File libraryFolder;

    public LibraryDownloadingTask(Library library, File libraryFolder) {
        this.library = library;
        this.libraryFolder = libraryFolder;
    }


    public void download(List<String> repositories) throws IOException {
        File output = library.getFile(libraryFolder);
        byte[] bytes = null;

        IOException exception = new IOException("Unable to download file " + output.getName() + ".");
        for (String repository : repositories) {
            String downloadUrl = repository + library.getUrl();
            LoggerProvider.logger.debug("Downloading from " + downloadUrl);
            try {
                bytes = getBytes(new URL(downloadUrl));
                break;
            } catch (Exception t) {
                exception.addSuppressed(t);
            }
        }

        if(bytes == null){
            throw exception;
        }

        if (!output.getParentFile().exists()) {
            Files.createDirectories(output.getParentFile().toPath());
        }

        LoggerProvider.logger.info("Downloaded " + output.getName());
        Files.write(output.toPath(), bytes);
    }

    private static byte[] getBytes(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.connect();
        httpURLConnection.disconnect();

        if (httpURLConnection.getResponseCode() == 200) {
            try (InputStream input = httpURLConnection.getInputStream()) {
                return input.readAllBytes();
            } finally {
                httpURLConnection.disconnect();
            }
        }
        throw new IOException(httpURLConnection.getResponseCode() + "");
    }
}
