package moe.caa.bukkit.multilogin;

import com.mojang.authlib.GameProfile;
import moe.caa.bukkit.multilogin.yggdrasil.MLGameProfile;
import moe.caa.bukkit.multilogin.yggdrasil.MLYggdrasilAuthenticationService;
import net.minecraft.server.v1_16_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R1.CraftServer;
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

    protected static void initService(MultiLogin plugin) throws ClassNotFoundException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException {
        Class CRAFT_PLAYER_CLASS = Class.forName("org.bukkit.craftbukkit." + NMS_VERSION + ".entity.CraftPlayer");
        Class ENTITY_PLAYER_CLASS = Class.forName("net.minecraft.server." + NMS_VERSION + ".EntityHuman");
        CRAFT_PLAYER_GET_HANDLE = CRAFT_PLAYER_CLASS.getDeclaredMethod("getHandle");
        ENTITY_HUMAN_GET_PROFILE = ENTITY_PLAYER_CLASS.getDeclaredMethod("getProfile");

        Class clazz = MinecraftServer.class;
        Field field = clazz.getDeclaredField("minecraftSessionService");
        field.setAccessible(true);
        field.set(((CraftServer)Bukkit.getServer()).getHandle().getServer(), new MLYggdrasilAuthenticationService(plugin).createMinecraftSessionService());
    }

    public static GameProfile getGameProfile(Player player) throws InvocationTargetException, IllegalAccessException {
        Object obj = CRAFT_PLAYER_GET_HANDLE.invoke(player);
        return  (MLGameProfile) ENTITY_HUMAN_GET_PROFILE.invoke(obj);
    }
}
