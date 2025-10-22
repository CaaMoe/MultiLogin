package moe.caa.multilogin.common.internal.main;

import moe.caa.multilogin.common.internal.Platform;
import moe.caa.multilogin.common.internal.config.MainConfig;
import moe.caa.multilogin.common.internal.config.MessageConfig;
import moe.caa.multilogin.common.internal.database.DatabaseHandler;
import moe.caa.multilogin.common.internal.profile.ProfileManager;
import moe.caa.multilogin.common.internal.util.IOUtil;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MultiCore {
    public final MessageConfig messageConfig = new MessageConfig();
    public final MainConfig mainConfig = new MainConfig();
    public final Platform platform;
    public final DatabaseHandler databaseHandler = new DatabaseHandler(this);
    public final ProfileManager profileManager = new ProfileManager(this);
    public final Executor asyncExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
            .name("MultiLogin Async #", 0)
            .factory());

    public MultiCore(Platform platform) {
        this.platform = platform;
    }

    public void load() throws IOException {
        reload();

        databaseHandler.initDatabase();
    }

    public void unload() {
        databaseHandler.close();
    }

    public void reload() throws IOException {
        Path configPath = platform.getPlatformConfigPath();
        if (!Files.exists(configPath)) {
            Files.createDirectories(configPath);
        }

        {
            Path messagesConfigPath = configPath.resolve("messages.conf");
            byte[] messagesNestResource = Objects.requireNonNull(IOUtil.readNestResource("messages.conf"), "Default messages.conf resource is missing!");
            if (!Files.exists(messagesConfigPath)) {
                Files.write(messagesConfigPath, messagesNestResource);
            }
            messageConfig.loadFrom(
                    HoconConfigurationLoader.builder().path(messagesConfigPath).build().load(),
                    HoconConfigurationLoader.builder().source(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messagesNestResource), StandardCharsets.UTF_8))).build().load()
            );
        }

        {
            Path mainConfigConfigPath = configPath.resolve("config.conf");
            byte[] mainConfigNestResource = Objects.requireNonNull(IOUtil.readNestResource("config.conf"), "Default config.conf resource is missing!");
            if (!Files.exists(mainConfigConfigPath)) {
                Files.write(mainConfigConfigPath, mainConfigNestResource);
            }
            mainConfig.loadFrom(
                    HoconConfigurationLoader.builder().path(mainConfigConfigPath).build().load(),
                    HoconConfigurationLoader.builder().source(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mainConfigNestResource), StandardCharsets.UTF_8))).build().load()
            );
        }
    }
}
