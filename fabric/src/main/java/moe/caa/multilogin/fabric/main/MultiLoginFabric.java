package moe.caa.multilogin.fabric.main;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.HasJoinedMinecraftServerResponse;
import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import moe.caa.multilogin.fabric.auth.MultiLoginYggdrasilMinecraftSessionService;
import moe.caa.multilogin.fabric.impl.FabricSender;
import moe.caa.multilogin.fabric.impl.ScheduleManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MultiLoginFabric implements IPlugin {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();
    public static Gson authGson;
    public static MultiLoginFabric plugin;
    private final MinecraftDedicatedServer server;
    private final MultiCore core;
    private final ScheduleManager schedule = new ScheduleManager();

    public MultiLoginFabric(MinecraftDedicatedServer server) {
        this.server = server;
        core = new MultiCore(this);
        MultiLoginFabric.plugin = this;
    }

    public boolean init() {
        return core.init();
    }

    @Override
    public File getDataFolder() {
        return new File("config/multilogin");
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return server.getPlayerManager().getPlayerList().stream().map(p -> new FabricSender(p.getCommandSource())).collect(Collectors.toList());
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    public String getPluginVersion() {
        return "@version@";
    }

    @Override
    public AbstractScheduler getSchedule() {
        return schedule;
    }

    @Override
    public boolean isOnlineMode() {
        return server.isOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        ServerPlayerEntity entity = server.getPlayerManager().getPlayer(uuid);
        if (entity == null) return null;
        return new FabricSender(entity.getCommandSource());
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (ServerPlayerEntity entity : server.getPlayerManager().getPlayerList()) {
            if (entity.getGameProfile().getName().equalsIgnoreCase(name)) {
                ret.add(new FabricSender(entity.getCommandSource()));
            }
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return authGson;
    }

    @Override
    public Type authResultType() {
        return HasJoinedMinecraftServerResponse.class;
    }

    @Override
    public void initCoreService() throws IllegalAccessException, NoSuchFieldException {
        Field field = ReflectUtil.getField(MinecraftServer.class, MinecraftSessionService.class, true);
        HttpMinecraftSessionService vanService = (HttpMinecraftSessionService) field.get(server);
        MultiLoginYggdrasilMinecraftSessionService mlymss = new MultiLoginYggdrasilMinecraftSessionService(vanService.getAuthenticationService(), core);
        mlymss.setVanService(vanService);
        field.set(server, mlymss);
    }

    @Override
    public void initOtherService() {

    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public MultiCore getMultiCore() {
        return core;
    }

    @Override
    public String getServerCoreName() {
        return server.getServerModName();
    }

    @Override
    public String getServerVersion() {
        return server.getVersion();
    }
}
