package moe.caa.multilogin.bukkit.main;

import moe.caa.multilogin.api.handle.HandleResult;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Bukkit 的事件处理程序
 */
public class GlobalListener {
    private final MultiLoginBukkit multiLoginBukkit;

    private final Listener listener = new Listener() {
        @EventHandler
        public void onJoin(AsyncPlayerPreLoginEvent event) {
            HandleResult result = multiLoginBukkit.getMultiCoreAPI().getPlayerHandler().pushPlayerJoinGame(event.getUniqueId(), event.getName());
            if (result.getType() == HandleResult.Type.KICK) {
                if (result.getKickMessage() == null || result.getKickMessage().trim().length() == 0) {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, "");
                } else {
                    event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, result.getKickMessage());
                }
            }
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            multiLoginBukkit.getMultiCoreAPI().getPlayerHandler().pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    };

    public GlobalListener(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
    }

    public void register() {
        multiLoginBukkit.getServer().getPluginManager().registerEvents(listener, multiLoginBukkit);
    }
}
