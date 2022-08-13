package moe.caa.multilogin.bukkit.main;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class GlobalListener {
    private final MultiLoginBukkit multiLoginBukkit;

    private final Listener listener = new Listener() {
        @EventHandler
        public void onJoin(AsyncPlayerPreLoginEvent event) {
            multiLoginBukkit.getMultiCoreAPI().getCache().pushPlayerJoinGame(event.getUniqueId(), event.getName());
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            multiLoginBukkit.getMultiCoreAPI().getCache().pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    };

    public GlobalListener(MultiLoginBukkit multiLoginBukkit) {
        this.multiLoginBukkit = multiLoginBukkit;
    }

    public void register() {
        multiLoginBukkit.getServer().getPluginManager().registerEvents(listener, multiLoginBukkit);
    }
}
