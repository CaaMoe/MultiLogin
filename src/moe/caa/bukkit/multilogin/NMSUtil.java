package moe.caa.bukkit.multilogin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import moe.caa.bukkit.multilogin.yggdrasil.MLGameProfile;
import moe.caa.bukkit.multilogin.yggdrasil.MLMultiYggdrasilAuthenticationService;
import moe.caa.bukkit.multilogin.yggdrasil.MLMultiYggdrasilMinecraftSessionService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class NMSUtil {
    public static final String NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    private static Method CRAFT_PLAYER_GET_HANDLE = null;
    private static Method ENTITY_HUMAN_GET_PROFILE = null;

    private NMSUtil(){
    }

    protected static void initService(MultiLogin plugin) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class CRAFT_PLAYER_CLASS = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer");
        Class ENTITY_PLAYER_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".EntityHuman");
        CRAFT_PLAYER_GET_HANDLE = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");
        ENTITY_HUMAN_GET_PROFILE = ENTITY_PLAYER_CLASS.getDeclaredMethod("getProfile");

        final Class CRAFT_SERVER_CLASS = Class.forName("org.bukkit.craftbukkit.v1_16_R1.CraftServer");
        final Method CRAFT_SERVER_GET_HANDLE = CRAFT_SERVER_CLASS.getDeclaredMethod("getHandle");

        final Class DEDICATED_PLAYER_LIST_CLASS = Class.forName("net.minecraft.server.v1_16_R1.DedicatedPlayerList");
        final Method DEDICATED_PLAYER_LIST_GET_HANDLE = DEDICATED_PLAYER_LIST_CLASS.getDeclaredMethod("getServer");

        final Class MINECRAFT_SERVER_CLASS = Class.forName("net.minecraft.server.v1_16_R1.MinecraftServer");

        Field field = MINECRAFT_SERVER_CLASS.getDeclaredField("minecraftSessionService");
        field.setAccessible(true);
        Object obj = DEDICATED_PLAYER_LIST_GET_HANDLE.invoke(CRAFT_SERVER_GET_HANDLE.invoke(Bukkit.getServer()));
        MinecraftSessionService vanServer = (MinecraftSessionService) field.get(obj);
        MLMultiYggdrasilAuthenticationService multiYggdrasilAuthenticationService = new MLMultiYggdrasilAuthenticationService();
        multiYggdrasilAuthenticationService.setVanService((YggdrasilAuthenticationService) vanServer);

        MLMultiYggdrasilMinecraftSessionService multiYggdrasilMinecraftSessionService = (MLMultiYggdrasilMinecraftSessionService) multiYggdrasilAuthenticationService.createMinecraftSessionService();
        multiYggdrasilMinecraftSessionService.setVanService((YggdrasilMinecraftSessionService) ((YggdrasilAuthenticationService) vanServer).createMinecraftSessionService());

        field.set(DEDICATED_PLAYER_LIST_GET_HANDLE, multiYggdrasilMinecraftSessionService);
    }

    public static GameProfile getGameProfile(Player player) throws InvocationTargetException, IllegalAccessException {
        Object obj = CRAFT_PLAYER_GET_HANDLE.invoke(player);
        return  (MLGameProfile) ENTITY_HUMAN_GET_PROFILE.invoke(obj);
    }
}
