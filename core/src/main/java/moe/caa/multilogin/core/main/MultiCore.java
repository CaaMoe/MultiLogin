package moe.caa.multilogin.core.main;

import lombok.Getter;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.core.auth.AuthHandler;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.configuration.PluginConfig;
import moe.caa.multilogin.core.database.SQLManager;
import moe.caa.multilogin.core.language.LanguageHandler;

import java.io.IOException;
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

    /**
     * 构建猫踢核心，这个方法将会被反射调用
     */
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        this.pluginConfig = new PluginConfig(plugin.getDataFolder());
        this.sqlManager = new SQLManager(this);
        this.authHandler = new AuthHandler(this);
        this.commandHandler = new CommandHandler(this);
        this.languageHandler = new LanguageHandler(this);
    }

    /**
     * 加载猫踢核心
     */
    @Override
    public void load() throws IOException, SQLException, ClassNotFoundException {
        pluginConfig.reload();
        sqlManager.init();
    }

    /**
     * 关闭猫踢核心
     */
    @Override
    public void close() {

    }
}
