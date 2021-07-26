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

import moe.caa.multilogin.bukkit.impl.BukkitSender;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Hashtable;
import java.util.Map;

public class BukkitListener implements Listener {
    public static final Map<Thread, String> AUTH_CACHE = new Hashtable<>();

    private final MultiCore core;

    public BukkitListener(MultiCore core) {
        this.core = core;
    }

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        String msg = AUTH_CACHE.remove(Thread.currentThread());
        if (msg != null) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(msg);
            return;
        }
        if (!MultiLoginBukkit.plugin.onAsyncLoginSuccess(event.getUniqueId(), event.getName())) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage());
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        MultiLoginBukkit.plugin.onQuit(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        MultiLoginBukkit.plugin.onJoin(new BukkitSender(event.getPlayer()));
    }
}
