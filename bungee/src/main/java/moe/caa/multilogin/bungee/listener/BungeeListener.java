package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {
    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!event.getConnection().isOnlineMode() || !(((InitialHandler) event.getConnection()).getLoginProfile() instanceof MultiLoginSignLoginResult)) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        if (MultiCore.isUpdate() && event.getPlayer().hasPermission("multilogin.update")) {
            event.getPlayer().sendMessage(new TextComponent(ChatColor.RED + "插件 " + ChatColor.YELLOW + "MultiLogin" + ChatColor.RED + " 有新的版本发布，请及时下载更新！"));
        }
    }
}
