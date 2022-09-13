package moe.caa.multilogin.fabric.main;

import com.mojang.brigadier.CommandDispatcher;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.fabric.command.MultiLoginCommand;
import moe.caa.multilogin.fabric.event.PluginEnableEvent;
import moe.caa.multilogin.fabric.event.PrepareAcceptLoginPlayerEvent;
import moe.caa.multilogin.fabric.impl.FabricServer;
import moe.caa.multilogin.fabric.inject.mixin.IServerLoginNetworkHandler_MLA;
import moe.caa.multilogin.fabric.inject.reflect.FabricInjector;
import moe.caa.multilogin.fabric.logger.Log4j2LoggerBridge;
import moe.caa.multilogin.fabric.logger.Slf4jLoggerBridge;
import moe.caa.multilogin.loader.main.PluginLoader;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.io.File;

/**
 * Fabric 主类
 */
@Environment(EnvType.SERVER)
public class MultiLoginFabric implements DedicatedServerModInitializer, IPlugin {
    @Getter
    private MinecraftServer server;
    @Getter
    private FabricServer runServer;
    private File dataFolder;
    private PluginLoader pluginLoader;
    @Getter
    private MultiCoreAPI api;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STOPPING.register(MultiLoginFabric.this::onDisable);
        CommandRegistrationCallback.EVENT.register(MultiLoginFabric.this::onRegisterCommand);

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> preparePlayerQuit(handler));
        PrepareAcceptLoginPlayerEvent.INSTANCE.register(MultiLoginFabric.this::prepareAcceptLoginPlayer);
        PluginEnableEvent.INSTANCE.register(MultiLoginFabric.this::onLEnable);
    }

    private void preparePlayerQuit(ServerPlayNetworkHandler handler) {
        api.getPlayerHandler().pushPlayerQuitGame(
                handler.getPlayer().getGameProfile().getId(),
                handler.getPlayer().getGameProfile().getName()
        );
    }

    private void prepareAcceptLoginPlayer(PrepareAcceptLoginPlayerEvent.EventData eventData) {
        IServerLoginNetworkHandler_MLA handlerMla = (IServerLoginNetworkHandler_MLA) eventData.serverLoginNetworkHandler();
        api.getPlayerHandler().pushPlayerJoinGame(handlerMla.mlHandler_getGameProfile().getId(), handlerMla.mlHandler_getGameProfile().getName());
    }

    private void onRegisterCommand(CommandDispatcher<ServerCommandSource> dispatcher,
                                   CommandRegistryAccess commandRegistryAccess,
                                   CommandManager.RegistrationEnvironment environment) {
        if (!environment.dedicated) {
            return;
        }
        new MultiLoginCommand(this).register(dispatcher);
    }

    private void onDisable(MinecraftServer server) {
        try {
            api.close();
            pluginLoader.close();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
        } finally {
            api = null;
            server.stop(false);
        }
    }

    private void onLEnable(MinecraftServer server) {
        MultiLoginFabric.this.server = server;
        this.runServer = new FabricServer(server);
        this.dataFolder = new File("config/multilogin");
        loggerInit();

        this.pluginLoader = new PluginLoader(this);
        try {
            pluginLoader.load();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while initializing the plugin.", e);
            server.stop(false);
            return;
        }

        try {
            api = pluginLoader.getCoreObject();
            api.load();
            new FabricInjector().inject(api);
        } catch (Throwable e) {
            LoggerProvider.getLogger().error("An exception was encountered while loading the plugin.", e);
            server.stop(false);
            return;
        }
    }

    private void loggerInit() {
        try {
            Class.forName("org.slf4j.LoggerFactory");
            Slf4jLoggerBridge.register();
        } catch (ClassNotFoundException e) {
            Log4j2LoggerBridge.register();
        }
    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    @Override
    public File getTempFolder() {
        return new File(getDataFolder(), "tmp");
    }
}
