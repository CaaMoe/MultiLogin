package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.HasJoinedResponseSerializer;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.PropertySerializer;
import moe.caa.multilogin.core.data.config.AdvancedSetting;
import moe.caa.multilogin.core.data.config.GeneralConfig;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.IOUtil;
import moe.caa.multilogin.core.util.YamlReader;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

import java.io.File;
import java.io.IOException;

@Getter
public class MultiCore {
    @Getter
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(HasJoinedResponse.class, new HasJoinedResponseSerializer())
            .registerTypeAdapter(Property.class, new PropertySerializer()).create();

    private final IPlugin plugin;
    private final MultiLogger logger;
    private final LanguageHandler languageHandler;
    private final YggdrasilServicesHandler yggdrasilServicesHandler;
    private final SQLManager sqlManager;
    private final GeneralConfig config;
    private final AdvancedSetting setting;

    private final File generalConfig;

    /**
     * 构建插件核心
     *
     * @param plugin 插件实例
     */
    @SneakyThrows
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        generalConfig = new File(plugin.getDataFolder(), "config.yml");
        setting = new AdvancedSetting(IOUtil.getJarResource("advanced_setting.properties"));
        config = new GeneralConfig();
        logger = new MultiLogger(this, true);
        languageHandler = new LanguageHandler();
        yggdrasilServicesHandler = new YggdrasilServicesHandler();
        sqlManager = new SQLManager(this);
    }

    public void init() throws IOException {
        genFile();
        config.reader(generalConfig);
        logger.init();
        languageHandler.init(this, "message.properties");
        yggdrasilServicesHandler.init(config.getReader().get("services", YamlReader.class));
        sqlManager.init(config.getReader().get("sql", YamlReader.class));

        logger.log(LoggerLevel.INFO, "插件已加载");
    }

    public void genFile() throws IOException {
        File folder = plugin.getDataFolder();
        IOUtil.createNewFileOrFolder(folder, true);

        IOUtil.saveResource(IOUtil.getJarResource("config.yml"), generalConfig, false);
    }
}
