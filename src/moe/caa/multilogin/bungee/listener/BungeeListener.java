package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.MultiInitialHandler;
import moe.caa.multilogin.bungee.RefUtil;
import moe.caa.multilogin.core.PluginData;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        if (event.getConnection().getClass() != MultiInitialHandler.class) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
        }
    }

    @EventHandler
    public void onLogin(LoginEvent event){
        if (!event.getConnection().isOnlineMode()) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
        }
    }

    @EventHandler
    public void onTab(TabCompleteEvent event){
        String cmd = event.getCursor();
    }
}
