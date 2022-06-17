package moe.caa.multilogin.core.configuration;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.util.IOUtil;
import moe.caa.multilogin.core.configuration.backend.BackendConfig;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginConfig {
    private final File dataFolder;
    @Getter
    private BackendConfig backendConfig;

    @Getter
    private Map<Integer, YggdrasilServiceConfig> yggdrasilServiceMap;

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

        saveResource("backend.yml", false);
        saveResource("service_template.yml", false);

        File backendFile = new File(dataFolder, "backend.yml");
        this.backendConfig = YamlConfigurationLoader.builder()
                .file(backendFile)
                .defaultOptions(opts -> opts.serializers(builder -> builder.register(BackendConfig.class, BackendConfig.BackendConfigSerializers.getInstance())))
                .build().load().get(BackendConfig.class);
        // 数据库配置已经读死了，在这里不会被第二次重载

        List<YggdrasilServiceConfig> tmpServices = new ArrayList<>();
        try (Stream<Path> list = Files.list(servicesFolder.toPath())) {
            for (Path path : list.collect(Collectors.toList())) {
                try {
                    YggdrasilServiceConfig config = YamlConfigurationLoader.builder()
                            .path(path)
                            .defaultOptions(opts -> opts.serializers(builder -> {
                                builder.register(YggdrasilServiceConfig.class, YggdrasilServiceConfig.YggdrasilServiceConfigSerializers.getInstance());
                                builder.register(ProxyConfig.class, ProxyConfig.ProxyConfigSerializers.getInstance());
                            }))
                            .build().load().get(YggdrasilServiceConfig.class);
                    tmpServices.add(config);
                } catch (Exception e) {
                    LoggerProvider.getLogger().error("Unable to read yggdrasil service in configuration path " + path, e);
                }
            }
        }

        Map<Integer, YggdrasilServiceConfig> tmpServiceMapById = new HashMap<>();
        for (YggdrasilServiceConfig service : tmpServices) {
            if (tmpServiceMapById.containsKey(service.getId())) {
                throw new ConfException("Duplicate yggdrasil service id value " + service.getId());
            }
            tmpServiceMapById.put(service.getId(), service);
        }

        if (tmpServiceMapById.size() == 0) {
            LoggerProvider.getLogger().warn("No yggdrasil are added.");
        } else {
            LoggerProvider.getLogger().info(tmpServiceMapById.size() + " servers have been added.");
        }
        yggdrasilServiceMap = Collections.unmodifiableMap(tmpServiceMapById);
    }

    public void saveResource(String path, boolean cover) throws IOException {
        File file = new File(dataFolder, path);
        if (file.exists() && !cover) {
            return;
        } else {
            Files.createFile(file.toPath());
        }
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream("/" + path));
             FileOutputStream fs = new FileOutputStream(file)) {
            IOUtil.copy(is, fs);
        }
    }
}
