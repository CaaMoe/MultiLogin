package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.main.BungeeSender;
import moe.caa.multilogin.bungee.main.MultiLoginBungee;
import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.language.LanguageKeys;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;

public class BungeeListener implements Listener {

    @EventHandler
    public void onLogin(LoginEvent event) {
        if (!event.getConnection().isOnlineMode() || !(((InitialHandler) event.getConnection()).getLoginProfile() instanceof MultiLoginSignLoginResult)) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage()));
        }
        if (!MultiLoginBungee.plugin.onAsyncLoginSuccess(event.getConnection().getUniqueId(), event.getConnection().getName())) {
            event.setCancelled(true);
            event.setCancelReason(new TextComponent(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage()));
        }
    }

    @EventHandler
    public void onSwitch(ServerSwitchEvent event) {
        MultiLoginBungee.plugin.onJoin(new BungeeSender(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        MultiLoginBungee.plugin.onRefreshCacheUserData();
    }
}