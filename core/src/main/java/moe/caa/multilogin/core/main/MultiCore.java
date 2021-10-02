package moe.caa.multilogin.core.main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.response.Property;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.HasJoinedResponseSerializer;
import moe.caa.multilogin.core.auth.yggdrasil.serialize.PropertySerializer;
import moe.caa.multilogin.core.data.database.SQLManager;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.language.LanguageHandler;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.yggdrasil.YggdrasilServicesHandler;

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

    /**
     * 构建插件核心
     *
     * @param plugin 插件实例
     */
    public MultiCore(IPlugin plugin) {
        this.plugin = plugin;
        logger = new MultiLogger(this, true);
        languageHandler = new LanguageHandler();
        yggdrasilServicesHandler = new YggdrasilServicesHandler();
        sqlManager = new SQLManager(this);
    }
}
