package moe.caa.multilogin.paper.internal.main;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import moe.caa.multilogin.common.internal.Platform;
import moe.caa.multilogin.common.internal.logger.KLogger;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.service.LocalYggdrasilSessionService;
import moe.caa.multilogin.paper.internal.channel.ChannelInjector;
import moe.caa.multilogin.paper.internal.manager.PaperCommandManager;
import moe.caa.multilogin.paper.internal.manager.PaperOnlinePlayerManager;
import moe.caa.multilogin.paper.internal.service.VanillaLocalYggdrasilSessionService;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class MultiLoginPaperMain extends JavaPlugin implements Platform {
    private static MultiLoginPaperMain instance;
    private final ChannelInjector channelInjector = new ChannelInjector(this);
    private final KLogger logger = new KLogger(getComponentLogger());
    private final MultiCore core = new MultiCore(this);
    private final PaperCommandManager commandManager = new PaperCommandManager(core);
    private final PaperOnlinePlayerManager onlinePlayerManager = new PaperOnlinePlayerManager(core);
    private final VanillaLocalYggdrasilSessionService vanillaLocalYggdrasilSessionService = new VanillaLocalYggdrasilSessionService(this);


    public static MultiLoginPaperMain getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        try {
            core.load();

            getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                event.registrar().register(commandManager.buildCommand());
            });
        } catch (Throwable t) {
            throw new RuntimeException("Failed to enable MultiLogin(paper).", t);
        }
    }

    @Override
    public void onDisable() {
        try {
            channelInjector.uninject();
            core.unload();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to disable MultiLogin(paper).", t);
        }
    }

    @Override
    public KLogger getPlatformLogger() {
        return logger;
    }

    @Override
    public Path getPlatformDataPath() {
        return getDataPath();
    }

    @Override
    public Path getPlatformConfigPath() {
        return getDataPath();
    }

    @Override
    public void onLoad() {
        instance = this;
        logger.checkLogAsDebugFlag(getDataPath());
        try {
            channelInjector.inject();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load MultiLogin(paper).", t);
        }
    }

    public MultiCore getCore() {
        return core;
    }

    @Override
    public PaperOnlinePlayerManager getOnlinePlayerManager() {
        return onlinePlayerManager;
    }

    @Override
    public LocalYggdrasilSessionService getLocalYggdrasilSessionService() {
        return vanillaLocalYggdrasilSessionService;
    }

    @Override
    public String getPluginVersion() {
        return getPluginMeta().getVersion();
    }

    @Override
    public String getServerName() {
        return getServer().getName();
    }

    @Override
    public String getServerVersion() {
        return getServer().getVersion();
    }
}
