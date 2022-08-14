package fun.ksnb.multilogin.bungee.main;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Bungee 的事件处理程序
 */
public class GlobalListener {
    private final MultiLoginBungee multiLoginBungee;

    private final Listener listener = new Listener() {

        @EventHandler
        public void onJoin(PostLoginEvent event) {
            multiLoginBungee.getMultiCoreAPI().getCache().pushPlayerJoinGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }

        @EventHandler
        public void onQuit(PlayerDisconnectEvent event) {
            multiLoginBungee.getMultiCoreAPI().getCache().pushPlayerQuitGame(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        }
    };

    public GlobalListener(MultiLoginBungee multiLoginBungee) {
        this.multiLoginBungee = multiLoginBungee;
    }

    public void register() {
        multiLoginBungee.getProxy().getPluginManager().registerListener(multiLoginBungee, listener);
    }
}
