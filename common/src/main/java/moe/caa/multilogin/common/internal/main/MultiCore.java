package moe.caa.multilogin.common.internal.main;

import moe.caa.multilogin.common.internal.Platform;
import moe.caa.multilogin.common.internal.config.MessageConfig;
import moe.caa.multilogin.common.internal.util.IOUtil;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class MultiCore {
    public final MessageConfig messageConfig = new MessageConfig();
    public final Platform platform;

    public MultiCore(Platform platform) {
        this.platform = platform;
    }

    public void load() throws IOException {
        reload();
    }

    public void unload() {

    }

    public void reload() throws IOException {
        Path configPath = platform.getPlatformConfigPath();
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath);
        }

        Path messagesConfigPath = configPath.resolve("messages.conf");
        if (!Files.exists(messagesConfigPath)) {
            byte[] resource = Objects.requireNonNull(IOUtil.readNestResource("messages.conf"), "Default messages.conf resource is missing!");
            Files.write(messagesConfigPath, resource);
        }
        messageConfig.loadFrom(HoconConfigurationLoader.builder().path(messagesConfigPath).build().load());
    }
}
