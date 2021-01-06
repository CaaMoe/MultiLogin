package moe.caa.multilogin.bukkit;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.bukkit.yggdrasil.MLGameProfile;
import moe.caa.multilogin.bukkit.yggdrasil.MLMultiYggdrasilAuthenticationService;
import moe.caa.multilogin.bukkit.yggdrasil.MLMultiYggdrasilMinecraftSessionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtil {
    public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static Method CRAFT_PLAYER_GET_HANDLE = null;
    private static Method ENTITY_HUMAN_GET_PROFILE = null;
    private static Gson authGson;

    private NMSUtil(){
    }

    protected static void initService(MultiLogin plugin) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<?> CRAFT_PLAYER_CLASS = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer");
        Class<?> ENTITY_PLAYER_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".EntityHuman");
        CRAFT_PLAYER_GET_HANDLE = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");
        ENTITY_HUMAN_GET_PROFILE = ENTITY_PLAYER_CLASS.getDeclaredMethod("getProfile");
        final Class<?> CRAFT_SERVER_CLASS = Class.forName("org.bukkit.craftbukkit."  + NMS_VERSION + ".CraftServer");
        final Method CRAFT_SERVER_GET_HANDLE = CRAFT_SERVER_CLASS.getDeclaredMethod("getHandle");
        final Class<?> DEDICATED_PLAYER_LIST_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".DedicatedPlayerList");
        final Method DEDICATED_PLAYER_LIST_GET_HANDLE = DEDICATED_PLAYER_LIST_CLASS.getDeclaredMethod("getServer");
        final Class<?> MINECRAFT_SERVER_CLASS = Class.forName("net.minecraft.server."  + NMS_VERSION + ".MinecraftServer");
        Field field = getField(MINECRAFT_SERVER_CLASS, MinecraftSessionService.class);
        field.setAccessible(true);
        Object obj = DEDICATED_PLAYER_LIST_GET_HANDLE.invoke(CRAFT_SERVER_GET_HANDLE.invoke(Bukkit.getServer()));

        HttpMinecraftSessionService vanServer = (HttpMinecraftSessionService) field.get(obj);
        MLMultiYggdrasilAuthenticationService multiYggdrasilAuthenticationService = new MLMultiYggdrasilAuthenticationService();
        multiYggdrasilAuthenticationService.setVanService(vanServer.getAuthenticationService());
        MLMultiYggdrasilMinecraftSessionService multiYggdrasilMinecraftSessionService = (MLMultiYggdrasilMinecraftSessionService) multiYggdrasilAuthenticationService.createMinecraftSessionService();
        multiYggdrasilMinecraftSessionService.setVanService(vanServer);

        field.set(obj, multiYggdrasilMinecraftSessionService);

        authGson = multiYggdrasilAuthenticationService.getGson();
    }

    protected static Gson getAuthGson(){
        return authGson;
    }

    private static Field getField(Class<?> clazz, Class<?> target){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getType() == target){
                return field;
            }
        }
        return null;
    }

    public static GameProfile getGameProfile(Player player) throws InvocationTargetException, IllegalAccessException {
        Object obj = CRAFT_PLAYER_GET_HANDLE.invoke(player);
        return  (MLGameProfile) ENTITY_HUMAN_GET_PROFILE.invoke(obj);
    }
}
