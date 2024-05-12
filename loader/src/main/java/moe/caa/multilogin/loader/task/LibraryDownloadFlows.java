package moe.caa.multilogin.loader.task;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.util.IOUtil;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;
import moe.caa.multilogin.loader.exception.InitialFailedException;
import moe.caa.multilogin.loader.library.Library;
import moe.caa.multilogin.loader.main.PluginLoader;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * 表示文件依赖下载流
 */
public class LibraryDownloadFlows extends BaseFlows<Void> {
    private final Library library;
    private final File librariesFolder;
    private final File tempLibrariesFolder;

    public LibraryDownloadFlows(Library library, File librariesFolder, File tempLibrariesFolder) {
        this.library = library;
        this.librariesFolder = librariesFolder;
        this.tempLibrariesFolder = tempLibrariesFolder;
    }

    private static byte[] getBytes(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.setConnectTimeout(10000);
        httpURLConnection.connect();

        if (httpURLConnection.getResponseCode() == 200) {
            try (InputStream input = httpURLConnection.getInputStream();
                 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                IOUtil.copy(input, output);
                return output.toByteArray();
            }
        }
        throw new IOException(httpURLConnection.getResponseCode() + "");
    }

    @Override
    public Signal run(Void unused) {
        File output = new File(librariesFolder, library.getFileName());
        File tmp = new File(tempLibrariesFolder, library.getFileName());
        byte[] bytes = null;

        List<Exception> exceptions = new ArrayList<>();
        for (String repository : PluginLoader.repositories) {
            String downloadUrl = repository + library.getDownloadUrl();
            LoggerProvider.getLogger().debug("Downloading from " + downloadUrl);
            try {
                bytes = getBytes(new URL(downloadUrl));
                break;
            } catch (Exception t) {
                final String cause = String.format("Download from %s failed.", downloadUrl);
                exceptions.add(new InitialFailedException(cause, t));
            }
        }

        if (bytes == null) {
            final String cause = String.format("Unable to download file %s.", library.getFileName());
            exceptions.forEach(e -> LoggerProvider.getLogger().error(new InitialFailedException(cause, e)));
            return Signal.TERMINATED;
        }

        try {
            if (!tmp.exists()) {
                Files.createFile(tmp.toPath());
            } else {
                try (FileWriter fw = new FileWriter(tmp)) {
                    fw.write("");
                    fw.flush();
                }
            }
            if (output.exists()) {
                Files.delete(output.toPath());
            }

            Files.write(tmp.toPath(), bytes);
            Files.move(tmp.toPath(), output.toPath());
            LoggerProvider.getLogger().info("Downloaded " + output.getName());
        } catch (Throwable t) {
            LoggerProvider.getLogger().error("Unable to process file " + library.getFileName(), t);
            return Signal.TERMINATED;
        }

        return Signal.PASSED;
    }
}
