/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.listener.BukkitListener
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.listener;

import moe.caa.multilogin.bukkit.impl.MultiLoginBukkit;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.databse.handler.UserDataHandler;
import moe.caa.multilogin.core.util.I18n;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import static moe.caa.multilogin.core.data.data.PluginData.configurationConfig;

public class BukkitListener implements Listener {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        String msg = AUTH_CACHE.remove(Thread.currentThread());
        if (msg != null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(msg);
            return;
        }
        if (MultiLoginBukkit.LOGIN_CACHE.remove(event.getUniqueId()) == null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(configurationConfig.getString("msgNoAdopt").get());
        }

        try {
            MultiLoginBukkit.USER_CACHE.put(event.getUniqueId(), UserDataHandler.getUserEntryByRedirectUuid(event.getUniqueId()));
        } catch (SQLException e) {
            e.printStackTrace();
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(configurationConfig.getString("msgNoAdopt").get());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        MultiCore.getPlugin().runTask(() -> {
            if (Bukkit.getPlayer(uuid) == null) {
                MultiLoginBukkit.USER_CACHE.remove(uuid);
            }
        });
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (MultiCore.isUpdate()) {
            if (event.getPlayer().isOp() || event.getPlayer().hasPermission("multilogin.update")) {
                event.getPlayer().sendMessage(I18n.getTransString("plugin_new_version_game"));
            }
        }
    }
}
