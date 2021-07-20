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
import moe.caa.multilogin.fabric.schedule.ScheduleManager;
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
    public static Gson authGson;
    public static Map<Thread, String> AUTH_CACHE = new Hashtable<>();
    private final MultiCore core = new MultiCore(this);
    private final MinecraftServer server;

    public MultiLoginFabric(MinecraftServer server) {
        this.server = server;
    }

    public void init() {
        if(!core.init()) core.disable();
    }


    @Override
    public File getDataFolder() {
        return new File("config/multilogin");
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return server.getPlayerManager().getPlayerList().stream().map(ServerPlayerEntity::getCommandSource).map(FabricSender::new).collect(Collectors.toList());
    }

    @Override
    public Logger getLogger() {
        //Logger.getGlobal();
        return Logger.getGlobal();
    }

    @Override
    public String getPluginVersion() {
        return "1.0-RC.1";
    }

    @Override
    public AbstractScheduler getSchedule() {
        return new ScheduleManager();
    }

    @Override
    public boolean isOnlineMode() {
        return server.isOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        ServerPlayerEntity entity = server.getPlayerManager().getPlayer(uuid);
        if(entity != null){
            return new FabricSender(entity.getCommandSource());
        }
        return null;
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        server.getPlayerManager().getPlayerList().forEach(entity -> {
            if(entity.getGameProfile().getName().equalsIgnoreCase(name)){
                ret.add(new FabricSender(entity.getCommandSource()));
            }
        });
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
    public void initCoreService() throws NoSuchFieldException, IllegalAccessException {
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
        if(server instanceof MinecraftDedicatedServer){
            ((MinecraftDedicatedServer) server).shutdown();
        }
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
