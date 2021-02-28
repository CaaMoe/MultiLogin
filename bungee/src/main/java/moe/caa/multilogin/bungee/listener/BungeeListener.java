package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.data.PluginData;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
    @EventHandler
    public void onLogin(LoginEvent event){
        if (!event.getConnection().isOnlineMode() || !(((InitialHandler) event.getConnection()).getLoginProfile() instanceof MultiLoginSignLoginResult)) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
        }
    }
}
