package moe.caa.multilogin.common.internal.main;

import moe.caa.multilogin.common.internal.Platform;
import moe.caa.multilogin.common.internal.config.LocalAuthenticationConfig;
import moe.caa.multilogin.common.internal.config.MainConfig;
import moe.caa.multilogin.common.internal.config.MessageConfig;
import moe.caa.multilogin.common.internal.config.RemoteAuthenticationConfig;
import moe.caa.multilogin.common.internal.database.DatabaseHandler;
import moe.caa.multilogin.common.internal.manager.LoginManager;
import moe.caa.multilogin.common.internal.manager.ProfileManager;
import moe.caa.multilogin.common.internal.manager.UserManager;
import moe.caa.multilogin.common.internal.util.IOUtil;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MultiCore {
    public final MessageConfig messageConfig = new MessageConfig();
    public final MainConfig mainConfig = new MainConfig();
    public final Platform platform;
    public final LoginManager loginManager = new LoginManager(this);
    public final DatabaseHandler databaseHandler = new DatabaseHandler(this);
    public final ProfileManager profileManager = new ProfileManager(this);
    public final UserManager userManager = new UserManager(this);
    public final Executor asyncExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual()
            .name("MultiLogin Async #", 0)
            .factory());

    public LocalAuthenticationConfig localAuthenticationConfig = null;
    public List<RemoteAuthenticationConfig> remoteAuthenticationConfigs = Collections.emptyList();


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

        Path remoteServicePath = configPath.resolve("remotes");
        if (!Files.exists(remoteServicePath)) {
            Files.createDirectories(remoteServicePath);
        }
        Files.write(remoteServicePath.resolve("example.conf"), Objects.requireNonNull(IOUtil.readNestResource("remote_auth_service_example.conf"), "Default remote_auth_service_example.conf resource is missing!"));


        LocalAuthenticationConfig localAuthenticationConfig = null;
        List<RemoteAuthenticationConfig> remoteAuthenticationConfigs = new ArrayList<>();


        Path messagesConfigPath = configPath.resolve("messages.conf");
        byte[] messagesNestResource = Objects.requireNonNull(IOUtil.readNestResource("messages.conf"), "Default messages.conf resource is missing!");
        if (!Files.exists(messagesConfigPath)) {
            Files.write(messagesConfigPath, messagesNestResource);
        }
        messageConfig.loadFrom(
                HoconConfigurationLoader.builder().path(messagesConfigPath).build().load(),
                HoconConfigurationLoader.builder().source(() -> new BufferedReader(new InputStreamReader(new ByteArrayInputStream(messagesNestResource), StandardCharsets.UTF_8))).build().load()
        );

        Path mainConfigConfigPath = configPath.resolve("config.conf");
        byte[] mainConfigNestResource = Objects.requireNonNull(IOUtil.readNestResource("config.conf"), "Default config.conf resource is missing!");
        if (!Files.exists(mainConfigConfigPath)) {
            Files.write(mainConfigConfigPath, mainConfigNestResource);
        }
        CommentedConfigurationNode configurationNode = HoconConfigurationLoader.builder().path(mainConfigConfigPath).build().load();
        mainConfig.loadFrom(configurationNode, HoconConfigurationLoader.builder().source(() ->
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mainConfigNestResource), StandardCharsets.UTF_8))
        ).build().load());

        MainConfig.AuthMode authMode = mainConfig.authMode.get();
        if (authMode == MainConfig.AuthMode.LOCAL || authMode == MainConfig.AuthMode.MIXED) {
            localAuthenticationConfig = new LocalAuthenticationConfig(mainConfig);
            localAuthenticationConfig.loadFrom(configurationNode.node("local-auth-service"));
        }
        if (authMode == MainConfig.AuthMode.REMOTE || authMode == MainConfig.AuthMode.MIXED) {
            try (Stream<Path> pathStream = Files.list(remoteServicePath)) {
                for (File file : pathStream.map(Path::toFile)
                        .filter(File::isFile)
                        .filter(it -> !it.getName().equalsIgnoreCase("example.conf")).toList()) {
                    RemoteAuthenticationConfig remoteAuthenticationConfig = new RemoteAuthenticationConfig(mainConfig);
                    remoteAuthenticationConfig.loadFrom(HoconConfigurationLoader.builder().file(file).build().load());
                    remoteAuthenticationConfigs.add(remoteAuthenticationConfig);
                }
            }
        }

        this.localAuthenticationConfig = localAuthenticationConfig;
        this.remoteAuthenticationConfigs = remoteAuthenticationConfigs;

        platform.getPlatformLogger().info("Authentication mode: " + authMode.name());
        if (localAuthenticationConfig != null) {
            platform.getPlatformLogger().info("");
        }
        for (RemoteAuthenticationConfig authenticationConfig : remoteAuthenticationConfigs) {
            platform.getPlatformLogger().info("Add");
        }
    }
}
