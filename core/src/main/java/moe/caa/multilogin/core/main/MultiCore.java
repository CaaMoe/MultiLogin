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
    private final PlayerCache cache;
    @Getter
    private final Gson gson;
    @Getter
    private final SemVersion semVersion;


    /**
     * 构建猫踢核心，这个方法将会被反射调用
     */
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        this.languageHandler = new LanguageHandler(this);
        this.pluginConfig = new PluginConfig(plugin.getDataFolder());
        this.sqlManager = new SQLManager(this);
        this.authHandler = new AuthHandler(this);
        this.commandHandler = new CommandHandler(this);
        this.semVersion = SemVersion.of(plugin.getVersion());
        this.cache = new PlayerCache(this);
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(GameProfile.class, new GameProfileSerializer())
                .registerTypeAdapter(Property.class, new PropertySerializer()).create();
    }

    /**
     * 加载猫踢核心
     */
    @Override
    public void load() throws IOException, SQLException, ClassNotFoundException, URISyntaxException {
        new CheckUpdater(this).start();
        new MetricsLite(this);
        new BuildManifest().read(this);
        languageHandler.init();
        pluginConfig.reload();
        sqlManager.init();
        commandHandler.init();
        cache.register();

        LoggerProvider.getLogger().info(
                String.format("Loaded, using MultiLogin v%s on %s v%s",
                        plugin.getVersion(), plugin.getRunServer().getName(), plugin.getRunServer().getVersion()
                )
        );
    }

    /**
     * 关闭猫踢核心
     */
    @Override
    public void close() {
        sqlManager.close();
    }
}
