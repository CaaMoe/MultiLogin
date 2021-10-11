package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.auth.BukkitAuthCore;
import moe.caa.multilogin.bukkit.impl.BukkitUserLogin;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkitPluginBootstrap;
import moe.caa.multilogin.core.impl.IPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class BukkitListener implements Listener {
    private final MultiLoginBukkitPluginBootstrap bootstrap;

    public BukkitListener(MultiLoginBukkitPluginBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {
        if (asyncPlayerPreLoginEvent.getUniqueId().equals(BukkitAuthCore.getDIRTY_UUID())) {
            for (BukkitUserLogin login : BukkitAuthCore.getLoginCachedHashSet().getEntrySet()) {
                if (login.getUsername().equals(asyncPlayerPreLoginEvent.getName())) {
                    asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, login.getKickMessage() == null ? "" : login.getKickMessage());
                    return;
                }
            }
            asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "请勿使用 UUID 为 " + BukkitAuthCore.getDIRTY_UUID() + " 的账户登入游戏");
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent playerJoinEvent) {
        BukkitAuthCore.getBufferUsers().getEntrySet().removeIf((user) -> {
            IPlayer player = bootstrap.getRunServer().getPlayerManager().getPlayer(user.getRedirectUuid());
            if (player != null) {
                BukkitAuthCore.getUsers().add(user);
                return true;
            }
            return false;
        });
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent playerQuitEvent) {
        BukkitAuthCore.getUsers().removeIf((user) ->
                bootstrap.getRunServer().getPlayerManager().getPlayer(user.getRedirectUuid()) == null
        );
    }
}
