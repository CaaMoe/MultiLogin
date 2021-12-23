package moe.caa.multilogin.bukkit.listener;

import lombok.AllArgsConstructor;
import moe.caa.multilogin.bukkit.auth.BukkitAuthCore;
import moe.caa.multilogin.bukkit.impl.BukkitUserLogin;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkitPluginBootstrap;
import moe.caa.multilogin.core.impl.IPlayer;
import moe.caa.multilogin.core.logger.LoggerLevel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class BukkitListener implements Listener {
    private final MultiLoginBukkitPluginBootstrap bootstrap;

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {
        if (asyncPlayerPreLoginEvent.getUniqueId().equals(BukkitAuthCore.getDIRTY_UUID())) {
            for (BukkitUserLogin login : BukkitAuthCore.getLoginCachedHashSet().getEntrySet()) {
                if (login.getUsername().equals(asyncPlayerPreLoginEvent.getName())) {
                    asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, login.getKickMessage() == null ? "请不要使用以下游戏内 UUID 登入游戏\n该 UUID 作为识别字段，请联系管理员更改掉您的游戏内 UUID\n\n" + BukkitAuthCore.getDIRTY_UUID() : login.getKickMessage());
                    return;
                }
            }
            asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "请不要使用以下游戏内 UUID 登入游戏\n该 UUID 作为识别字段，请联系管理员更改掉您的游戏内 UUID\n\n" + BukkitAuthCore.getDIRTY_UUID());
        } else {
            for (BukkitUserLogin login : BukkitAuthCore.getLoginCachedHashSet().getEntrySet()) {
                if (login.getUsername().equals(asyncPlayerPreLoginEvent.getName())) {
                    if (login.getKickMessage() != null) {
                        asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, login.getKickMessage());
                    }
                    return;
                }
            }
        }
        // asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, bootstrap.getCore().getLanguageHandler().getMessage("auth_bukkit_invalid_login"));
        bootstrap.getCore().getLogger().log(LoggerLevel.WARN, "Through an illegal user: " + asyncPlayerPreLoginEvent.getUniqueId() + "(" + asyncPlayerPreLoginEvent.getName() + ")");
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
