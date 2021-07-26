/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.listener.VelocityListener
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocity;
import fun.ksnb.multilogin.velocity.main.VelocitySender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import net.kyori.adventure.text.Component;

public class VelocityListener {


    private final MultiCore core;

    public VelocityListener(MultiCore core) {
        this.core = core;
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        if (!event.getPlayer().isOnlineMode() || !MultiLoginVelocity.getInstance().onAsyncLoginSuccess(event.getPlayer().getUniqueId(), event.getPlayer().getUsername())) {
            event.setResult(ResultedEvent.ComponentResult.denied(Component.text(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage(core))));
        }
    }

    @Subscribe
    public void onJoin(ServerConnectedEvent event) {
        MultiLoginVelocity.getInstance().onJoin(new VelocitySender(event.getPlayer()));
    }

    @Subscribe
    public void onQuit(DisconnectEvent event) {
        MultiLoginVelocity.getInstance().onQuit(event.getPlayer().getUniqueId());
    }
}
