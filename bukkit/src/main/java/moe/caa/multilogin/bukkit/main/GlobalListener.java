package moe.caa.multilogin.bukkit.main;


import moe.caa.multilogin.api.internal.handle.HandleResult;
import moe.caa.multilogin.bukkit.impl.BukkitPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;


/**
 * Bukkit 的事件处理程序
 */
public class GlobalListener implements Listener {
    private final MultiLoginBukkit multiLoginBukkit;


    public GlobalListener(MultiLoginBukkit multiLoginBungee) {
        this.multiLoginBukkit = multiLoginBungee;
    }

    @EventHandler
    public void onJoin(PlayerLoginEvent event) {
        HandleResult result = multiLoginBukkit.getMultiCoreAPI().getPlayerHandler().pushPlayerJoinGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (result.getType() == HandleResult.Type.KICK) {
            if (result.getKickMessage() == null || result.getKickMessage().trim().isEmpty()) {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "");
            } else {
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, result.getKickMessage());
            }
            return;
        }
        multiLoginBukkit.getMultiCoreAPI().getPlayerHandler().callPlayerJoinGame(new BukkitPlayer(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        multiLoginBukkit.getMultiCoreAPI().getPlayerHandler().pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }

    public void register() {
        multiLoginBukkit.getServer().getPluginManager().registerEvents(this, multiLoginBukkit);
    }
}
