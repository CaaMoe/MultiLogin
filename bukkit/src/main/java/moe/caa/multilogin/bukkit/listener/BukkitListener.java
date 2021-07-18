package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.BukkitSender;
import moe.caa.multilogin.bukkit.MultiLoginBukkit;
import moe.caa.multilogin.core.language.LanguageKeys;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Hashtable;
import java.util.Map;

public class BukkitListener implements Listener {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        String msg = AUTH_CACHE.remove(Thread.currentThread());
        if (msg != null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(msg);
            return;
        }
        if (!MultiLoginBukkit.plugin.onAsyncLoginSuccess(event.getUniqueId(), event.getName())) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        MultiLoginBukkit.plugin.onLeave();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        MultiLoginBukkit.plugin.onJoin(new BukkitSender(event.getPlayer()));
    }
}
