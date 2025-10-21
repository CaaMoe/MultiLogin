package moe.caa.multilogin.paper.main;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import moe.caa.multilogin.common.internal.Platform;
import moe.caa.multilogin.common.internal.command.CommandManager;
import moe.caa.multilogin.common.internal.command.SenderUnwrapper;
import moe.caa.multilogin.common.internal.logger.KLogger;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.paper.channel.ChannelInjector;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public class MultiLoginPaperMain extends JavaPlugin implements Platform {
    private final ChannelInjector channelInjector = new ChannelInjector(this);
    private final KLogger logger = new KLogger(getComponentLogger());
    private final MultiCore core = new MultiCore(this);
    private final CommandManager<CommandSourceStack> commandManager = new CommandManager<>(
            core, new SenderUnwrapper<>(
            (commandSourceStack, permission) -> commandSourceStack.getSender().hasPermission(permission),
            (commandSourceStack, component) -> commandSourceStack.getSender().sendMessage(component))
    );

    @Override
    public void onLoad() {
        logger.checkLogAsDebugFlag(getDataPath());
        try {
            channelInjector.inject();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to load MultiLogin(paper).", t);
        }
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

    public MultiCore getCore() {
        return core;
    }
}
