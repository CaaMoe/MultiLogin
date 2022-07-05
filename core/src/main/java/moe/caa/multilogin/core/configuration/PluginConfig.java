package moe.caa.multilogin.core.configuration;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;

public class PluginConfig {
    private final File dataFolder;

    @Getter
    private SqlConfig sqlConfig;

    public PluginConfig(File dataFolder) {
        this.dataFolder = dataFolder;
    }

    public void reload() throws IOException {
        File servicesFolder = new File(dataFolder, "services");
        if (!dataFolder.exists()) {
            Files.createDirectory(dataFolder.toPath());
        }
        if (!servicesFolder.exists()) {
            Files.createDirectory(servicesFolder.toPath());
        }

        saveResource("config.yml", false);
        saveResource("service_template.yml", true);


    }

    public void saveResource(String path, boolean cover) throws IOException {
        File file = new File(dataFolder, path);
        boolean exists = file.exists();
        if (exists && !cover) {
            return;
        } else {
            if (!exists) Files.createFile(file.toPath());
        }
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/" + path));
             FileOutputStream fs = new FileOutputStream(file)) {
            IOUtil.copy(is, fs);
        }
        if (!exists) {
            LoggerProvider.getLogger().info("Extract: " + path);
        } else {
            LoggerProvider.getLogger().info("Cover: " + path);
        }
    }
}
