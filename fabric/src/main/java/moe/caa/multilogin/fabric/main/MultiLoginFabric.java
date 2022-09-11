package moe.caa.multilogin.fabric.main;

import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.plugin.IPlugin;
import moe.caa.multilogin.fabric.event.PluginEnableEvent;
import moe.caa.multilogin.fabric.impl.FabricServer;
import moe.caa.multilogin.fabric.logger.Log4j2LoggerBridge;
import moe.caa.multilogin.fabric.logger.Slf4jLoggerBridge;
import moe.caa.multilogin.loader.main.PluginLoader;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

import java.io.File;

@Environment(EnvType.SERVER)
public class MultiLoginFabric implements DedicatedServerModInitializer, IPlugin {
    private MinecraftServer server;
    @Getter
    private FabricServer runServer;
    private File dataFolder;
    private PluginLoader pluginLoader;
    @Getter
    private MultiCoreAPI multiCoreAPI;

    @Override
    public void onInitializeServer() {
        PluginEnableEvent.INSTANCE.register(MultiLoginFabric.this::onLEnable);
        ServerLifecycleEvents.SERVER_STOPPING.register(MultiLoginFabric.this::onDisable);
    }

    private void onDisable(MinecraftServer server) {
        try {
            multiCoreAPI.close();
            pluginLoader.close();
        } catch (Exception e) {
            LoggerProvider.getLogger().error("An exception was encountered while close the plugin", e);
        } finally {
            multiCoreAPI = null;
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
            multiCoreAPI = pluginLoader.getCoreObject();
            multiCoreAPI.load();
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
