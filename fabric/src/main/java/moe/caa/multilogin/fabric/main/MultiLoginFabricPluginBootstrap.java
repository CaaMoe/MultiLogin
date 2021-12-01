package moe.caa.multilogin.fabric.main;

import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import moe.caa.multilogin.fabric.auth.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.fabric.impl.FabricServer;
import moe.caa.multilogin.fabric.loader.main.MultiLoginFabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import static moe.caa.multilogin.core.util.IOUtil.getJarResource;

public class MultiLoginFabricPluginBootstrap extends BasePluginBootstrap implements IPlugin {
    @Getter
    private static MultiLoginFabricPluginBootstrap instance;
    private final Logger logger = LogManager.getLogger("MultiLogin");
    private final String PLUGIN_VERSION = new JsonParser().parse(new InputStreamReader(getJarResource("fabric.mod.json"))).getAsJsonObject().get("version").getAsString();
    @Getter
    private final MultiCore core;

    private final FabricServer runServer;

    private final MinecraftServer server;
    private final MultiLoginFabricLoader fabricLoader;


    public MultiLoginFabricPluginBootstrap(MultiLoginFabricLoader fabricLoader, MinecraftServer server) {
        this.fabricLoader = fabricLoader;
        this.server = server;
        this.runServer = new FabricServer(server);
        this.core = new MultiCore(this);
    }


    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public void initService() throws Throwable {
        Field field = ReflectUtil.handleAccessible(ReflectUtil.getField(MinecraftServer.class, MinecraftSessionService.class), true);
        HttpMinecraftSessionService vanService = (HttpMinecraftSessionService) field.get(server);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanService.getAuthenticationService());
        mlymss.setVanService(vanService);
        mlymss.setBootstrap(this);
        field.set(server, mlymss);
    }

    @Override
    public void initOther() {

    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        if (level == LoggerLevel.ERROR) fabricLoader.getLogger().error(message, throwable);
        else if (level == LoggerLevel.WARN) fabricLoader.getLogger().warn(message, throwable);
        else if (level == LoggerLevel.INFO) fabricLoader.getLogger().info(message, throwable);
        else if (level == LoggerLevel.DEBUG) {
        } else fabricLoader.getLogger().info(message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return runServer;
    }

    @Override
    public File getDataFolder() {
        return fabricLoader.getDataFolder();
    }

    @Override
    public String getPluginVersion() {
        return PLUGIN_VERSION;
    }
}
