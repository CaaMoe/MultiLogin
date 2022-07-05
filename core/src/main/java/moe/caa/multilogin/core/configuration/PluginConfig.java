package moe.caa.multilogin.core.configuration;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.IOUtil;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

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
        saveResourceDir("examples", true);


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

    public void saveResourceDir(String path, boolean cover) throws IOException {
        File file = new File(dataFolder, path);
        if (!file.exists()) Files.createDirectory(file.toPath());
        try (JarFile jarFile = new JarFile(getClass().getProtectionDomain().getCodeSource().getLocation().getFile())) {
            List<JarEntry> jarFiles = jarFile.stream().filter(jarEntry -> jarEntry.getRealName().startsWith(path)).filter(jarEntry -> !jarEntry.getRealName().equals(path + "/")).collect(Collectors.toList());
            for (JarEntry je : jarFiles) {
//                if (je.isDirectory()) {
//                暂时不考虑目录下目录情况
//                    预留
//                } else {
//                下属文件
                    String realName=je.getRealName();
                    String fileName=realName.substring(path.length());
                    File subFile = new File(file, fileName);
                    boolean exists = subFile.exists();
                    if (exists && !cover) {
                        return;
                    } else {
                        if (!exists) Files.createFile(subFile.toPath());
                    }
                    try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/" +realName));
                         FileOutputStream fs = new FileOutputStream(subFile)) {
                        IOUtil.copy(is, fs);
                    }
                    if (!exists) {
                        LoggerProvider.getLogger().info("Extract: " + realName);
                    } else {
                        LoggerProvider.getLogger().info("Cover: " + realName);
                    }
//                }
            }
        }
    }
}
