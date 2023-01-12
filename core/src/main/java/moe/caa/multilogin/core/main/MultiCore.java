package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.auth.Property;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.auth.AuthHandler;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.GameProfileSerializer;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.PropertySerializer;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.configuration.PluginConfig;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.handle.CacheWhitelistHandler;
import moe.caa.multilogin.core.handle.PlayerHandler;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.semver.CheckUpdater;
import moe.caa.multilogin.core.semver.SemVersion;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * 猫踢核心
 */
public class MultiCore implements MultiCoreAPI {
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


    /**
     * 构建猫踢核心，这个方法将会被反射调用
     */
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        this.buildManifest = new BuildManifest(this);
        this.languageHandler = new LanguageHandler(this);
        this.pluginConfig = new PluginConfig(plugin.getDataFolder());
        this.sqlManager = new SQLManager(this);
        this.authHandler = new AuthHandler(this);
        this.commandHandler = new CommandHandler(this);
        this.playerHandler = new PlayerHandler(this);
        this.cacheWhitelistHandler = new CacheWhitelistHandler();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(GameProfile.class, new GameProfileSerializer())
                .registerTypeAdapter(Property.class, new PropertySerializer()).create();
    }

    private void showBanner(){
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
        showBanner();
        buildManifest.read();
        buildManifest.checkStable();

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
    }

    private void checkEnvironment() {
        if (!plugin.getRunServer().isOnlineMode()) {
            LoggerProvider.getLogger().error("Please enable online mode, otherwise the plugin will not work!!!");
            LoggerProvider.getLogger().error("Server is closing!!!");
            throw new EnvironmentException("offline mode.");
        }
        if (!plugin.getRunServer().isForwarded()) {
            if (getPluginConfig().isDisableForwardingCheck()) {
                LoggerProvider.getLogger().warn("Please enable forwarding, otherwise the plugin will not work properly!!!");
                return;
            }
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
}
