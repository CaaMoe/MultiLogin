package moe.caa.multilogin.bukkit.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.Hashtable;
import java.util.Map;

public class BukkitListener implements Listener {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event){
        String msg = AUTH_CACHE.remove(Thread.currentThread());
        if(msg != null){
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(msg);
        }
    }
}
