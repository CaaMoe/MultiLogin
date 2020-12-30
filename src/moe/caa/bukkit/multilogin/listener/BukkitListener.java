package moe.caa.bukkit.multilogin.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class BukkitListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    private void onLogin(PlayerLoginEvent event){
        // TODO e...
    }
}
