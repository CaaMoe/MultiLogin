package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.MultiLogin;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.PluginData;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
    @EventHandler
    public void onLogin(LoginEvent event){
        if (!event.getConnection().isOnlineMode() || !MultiLogin.SAFE_CACHE.contains(event.getConnection().getUniqueId())) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event){
        if(MultiCore.isUpdate() && (event.getPlayer().hasPermission("multilogin.update"))){
            event.getPlayer().sendMessage(new TextComponent("§c插件 §eMultiLogin §c有新的版本发布，请及时下载更新！"));
        }
    }
}
