package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.MultiLoginBukkit;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.data.database.handler.UserDataHandler;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

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
        if (MultiLoginBukkit.LOGIN_CACHE.remove(event.getUniqueId()) == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        MultiCore.plugin.getSchedule().runTask(() -> {
            if (MultiCore.plugin.getPlayer(uuid) == null) {
                MultiLoginBukkit.USER_CACHE.remove(uuid);
            }
        });
    }
}
