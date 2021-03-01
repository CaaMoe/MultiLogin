package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;
import moe.caa.multilogin.core.MultiCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import static moe.caa.multilogin.core.data.data.PluginData.configurationConfig;

public class BukkitListener implements Listener {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event){
        String msg = AUTH_CACHE.remove(Thread.currentThread());
        if(msg != null){
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(msg);
            return;
        }
        if (MultiLoginBukkit.LOGIN_CACHE.remove(event.getUniqueId()) == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(configurationConfig.getString("msgNoAdopt"));
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event){
        UUID uuid = event.getPlayer().getUniqueId();
        MultiCore.getPlugin().runTask(()->{
            if(Bukkit.getPlayer(uuid) == null){
                MultiLoginBukkit.USER_CACHE.remove(uuid);
            }
        }, 20 * 60 * 30);
    }
}
