package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.library.LibrariesHandler;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.serialize.HasJoinedResponseSerializer;
import moe.caa.multilogin.core.serialize.PropertySerializer;
import moe.caa.multilogin.core.util.IOUtil;
import moe.caa.multilogin.core.util.YamlConfig;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.io.File;
import java.io.FileInputStream;

/**
 * 插件核心，衔接
 */
@Getter
public class MultiCore {
    @Getter
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(HasJoinedResponse.class, new HasJoinedResponseSerializer())
            .registerTypeAdapter(Property.class, new PropertySerializer())
            .create();
    @Getter
    private static MultiCore core;
    private final MultiLogger logger;
    private final LanguageHandler languageHandler;
    private final IPlugin plugin;
    private final LibrariesHandler librariesHandler;
    private final YggdrasilServicesHandler yggdrasilServicesHandler;
    private final SQLManager sqlManager;
    private final File configFile;
    private YamlConfig yamlConfig;
    private long servicesTimeOut;
    private boolean whitelist;
    private String nameAllowedRegular;

    /**
     * 构建插件核心
     *
     * @param plugin 插件实例
     */
    public MultiCore(IPlugin plugin) {
        MultiCore.core = this;
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        logger = new MultiLogger(this);
        librariesHandler = new LibrariesHandler(this, new File(plugin.getDataFolder(), "libraries"));
        languageHandler = new LanguageHandler();
        yggdrasilServicesHandler = new YggdrasilServicesHandler();
        sqlManager = new SQLManager(this);
    }

    /**
     * 初始化插件服务
     *
     * @return 初始化成功
     */
    public boolean init() {
        var ret = init0();
        if (!ret) logger.log(LoggerLevel.ERROR, "遇到致命性异常，插件将关闭");
        return ret;
    }

    /**
     * 初始化插件服务
     *
     * @return 初始化成功
     */
    private boolean init0() {
        try {
            if (!librariesHandler.init()) return false;

            IOUtil.saveResource(IOUtil.getJarResource("config.yml"), configFile, false);
            yamlConfig = YamlConfig.fromInputStream(new FileInputStream(configFile));
            var debug = yamlConfig.get("debug", Boolean.class, false);
            servicesTimeOut = yamlConfig.get("servicesTimeOut", Number.class, 10000).longValue();
            whitelist = yamlConfig.get("whitelist", Boolean.class, true);
            nameAllowedRegular = yamlConfig.get("nameAllowedRegular", String.class, "");

            logger.init(debug);

            if (!languageHandler.init(this, "message.properties")) return false;

            if (!plugin.getRunServer().getPlayerManager().isOnlineMode()) {
                logger.log(LoggerLevel.ERROR, "服务器是运行在离线模式下。");
                logger.log(LoggerLevel.WARN, "离线模式下插件将不会工作，请开启正版验证！");
                return false;
            }

            if (plugin.getRunServer().getPlayerManager().isWhitelist()) {
                logger.log(LoggerLevel.WARN, "服务器似乎开启了自带的白名单程序，有可能与此插件的白名单程序发生冲突！");
            }

            yggdrasilServicesHandler.init(yamlConfig.get("services", YamlConfig.class, YamlConfig.empty()));

            if (!sqlManager.init(yamlConfig.get("sql", YamlConfig.class, YamlConfig.empty()))) return false;


            plugin.initService();
            plugin.initOther();
        } catch (Throwable throwable) {
            logger.log(LoggerLevel.ERROR, "", throwable);
            return false;
        }
        return true;
    }

    public void disable() {
        plugin.getRunServer().getScheduler().shutdown();
        sqlManager.close();
        plugin.getRunServer().shutdown();
    }

    /**
     * 重新加载配置文件
     */
    public void reload() {
    }
}
