package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import moe.caa.multilogin.api.MapperConfigAPI;
import moe.caa.multilogin.api.MultiLoginAPI;
import moe.caa.multilogin.api.MultiLoginAPIProvider;
import moe.caa.multilogin.api.data.MultiLoginPlayerData;
import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.profile.Property;
import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.main.MultiCoreAPI;
import moe.caa.multilogin.api.internal.plugin.IPlugin;
import moe.caa.multilogin.api.service.IService;
import moe.caa.multilogin.core.auth.AuthHandler;
import moe.caa.multilogin.core.auth.service.floodgate.FloodgateAuthenticationService;
import moe.caa.multilogin.core.auth.service.yggdrasil.serialize.GameProfileSerializer;
import moe.caa.multilogin.core.auth.service.yggdrasil.serialize.PropertySerializer;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.configuration.MapperConfig;
import moe.caa.multilogin.core.configuration.PluginConfig;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.handle.CacheWhitelistHandler;
import moe.caa.multilogin.core.handle.PlayerHandler;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.semver.CheckUpdater;
import moe.caa.multilogin.core.semver.SemVersion;
import moe.caa.multilogin.core.skinrestorer.SkinRestorerCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 猫踢核心
 */
public class MultiCore implements MultiCoreAPI, MultiLoginAPI {
    @Getter
    private final IPlugin plugin;
    @Getter
    private final BuildManifest buildManifest;
    @Getter
    private final SQLManager sqlManager;
    @Getter
    private final PluginConfig pluginConfig;
    @Getter
    private final AuthHandler authHandler;
    @Getter
    private final SkinRestorerCore skinRestorerHandler;
    @Getter
    private final CommandHandler commandHandler;
    @Getter
    private final LanguageHandler languageHandler;
    @Getter
    private final PlayerHandler playerHandler;
    @Getter
    private final CacheWhitelistHandler cacheWhitelistHandler;
    @Getter
    private final Gson gson;
    @Getter
    private SemVersion semVersion;
    @Getter
    private boolean floodgateSupported = false;
    @Getter
    private final String httpRequestHeaderUserAgent = "MultiLogin/v2.0";

    /**
     * 构建猫踢核心，这个方法将会被反射调用
     */
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        this.buildManifest = new BuildManifest(this);
        this.languageHandler = new LanguageHandler(this);
        this.pluginConfig = new PluginConfig(plugin.getDataFolder(), this);
        this.sqlManager = new SQLManager(this);
        this.authHandler = new AuthHandler(this);
        this.skinRestorerHandler = new SkinRestorerCore(this);
        this.commandHandler = new CommandHandler(this);
        this.playerHandler = new PlayerHandler(this);
        this.cacheWhitelistHandler = new CacheWhitelistHandler();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(GameProfile.class, new GameProfileSerializer())
                .registerTypeAdapter(Property.class, new PropertySerializer()).create();
    }

    private void setupFloodgate() {
        if (plugin.getRunServer().pluginHasEnabled("floodgate")) {
            try {
                new FloodgateAuthenticationService(this).register();
                LoggerProvider.getLogger().info("Floodgate detected, service registered.");
                floodgateSupported = true;
            } catch (Throwable e) {
                floodgateSupported = false;
                LoggerProvider.getLogger().error("Unable to load floodgate handler, is it up to date?", e);
            }
        }
    }

    private void showBanner() {
        //show banner
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;31m __  __       _ _   _ _                _       \033[0m");
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;33m|  \\/  |_   _| | |_(_) |    ___   __ _(_)_ __  \033[0m");
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;32m| |\\/| | | | | | __| | |   / _ \\ / _` | | '_ \\ \033[0m");
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;36m| |  | | |_| | | |_| | |__| (_) | (_| | | | | |\033[0m");
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;34m|_|  |_|\\__,_|_|\\__|_|_____\\___/ \\__, |_|_| |_|\033[0m");
        plugin.getRunServer().getConsoleSender().sendMessagePL("\033[40;35m                                 |___/         \033[0m");
    }

    /**
     * 加载猫踢核心
     */
    @Override
    public void load() throws IOException, SQLException, ClassNotFoundException, URISyntaxException {
        MultiLoginAPIProvider.setApi(this);

        showBanner();
        buildManifest.read();
        buildManifest.checkStable();

        setupFloodgate();
        languageHandler.init();
        pluginConfig.reload();
        sqlManager.init();
        commandHandler.init();
        playerHandler.register();
        new CheckUpdater(this).start();


        this.semVersion = SemVersion.of(buildManifest.getVersion());
        LoggerProvider.getLogger().info(
                String.format("Loaded, using MultiLogin v%s on %s - %s",
                        buildManifest.getVersion(), plugin.getRunServer().getName(), plugin.getRunServer().getVersion()
                )
        );
        checkEnvironment();

        try {
            new MetricsLite(this);
        } catch (Throwable throwable){
            LoggerProvider.getLogger().error(throwable);
        }
    }

    private void checkEnvironment() {
        if (!plugin.getRunServer().isOnlineMode()) {
            LoggerProvider.getLogger().error("Please enable online mode, otherwise the plugin will not work!!!");
            LoggerProvider.getLogger().error("Server is closing!!!");
            throw new EnvironmentException("offline mode.");
        }
        if (!plugin.getRunServer().isForwarded()) {
            LoggerProvider.getLogger().error("Please enable forwarding, otherwise the plugin will not work!!!");
            LoggerProvider.getLogger().error("Server is closing!!!");
            throw new EnvironmentException("do not forward.");
        }
    }

    public void reload() throws IOException, URISyntaxException {
        pluginConfig.reload();
        languageHandler.reload();
    }

    /**
     * 关闭猫踢核心
     */
    @Override
    public void close() {
        sqlManager.close();
    }

    @Override
    public MapperConfigAPI getMapperConfig() {
        return pluginConfig.getMapperConfig();
    }

    @NotNull
    @Override
    public Collection<BaseServiceConfig> getServices() {
        return Collections.unmodifiableCollection(pluginConfig.getServiceIdMap().values());
    }

    @Nullable
    @Override
    public MultiLoginPlayerData getPlayerData(@NotNull UUID inGameUUID) {
        return playerHandler.getPlayerData(inGameUUID);
    }
}
