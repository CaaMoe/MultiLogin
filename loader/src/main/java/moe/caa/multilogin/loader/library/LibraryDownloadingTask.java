package moe.caa.multilogin.loader.library;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.loader.main.PluginLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class LibraryDownloadingTask {
    private final Library library;

    public LibraryDownloadingTask(Library library) {
        this.library = library;
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

    public void download(File output) throws IOException {
        byte[] bytes = null;

        IOException exception = new IOException("Unable to download file " + output.getName() + ".");
        for (String repository : PluginLoader.REPOSITORIES) {
            String downloadUrl = repository + library.getUrl();
            LoggerProvider.logger.debug("Downloading from " + downloadUrl);
            try {
                bytes = getBytes(new URL(downloadUrl));
                LoggerProvider.logger.info("Downloaded " + downloadUrl);
                break;
            } catch (IOException t) {
                exception.addSuppressed(t);
            }
        }

        if (bytes != null) {
            try {
                if (!output.getParentFile().exists()) {
                    Files.createDirectories(output.getParentFile().toPath());
                }
                Files.write(output.toPath(), bytes);
                return;
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
        throw exception;
    }
}
