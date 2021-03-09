/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.listener.BungeeListener
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.listener;

import moe.caa.multilogin.bungee.proxy.MultiLoginSignLoginResult;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.util.I18n;
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
            event.getPlayer().sendMessage(new TextComponent(I18n.getTransString("plugin_new_version_game")));
        }
    }
}
