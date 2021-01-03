package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.RefUtil;
import moe.caa.multilogin.core.PluginData;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        try {
            RefUtil.modify(event);
            event.getConnection().setOnlineMode(true);
        } catch (Exception e) {
            e.printStackTrace();
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
        }
    }
}
