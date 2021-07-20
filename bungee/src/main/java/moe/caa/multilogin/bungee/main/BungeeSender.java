/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.main.BungeeSender
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.main;

import moe.caa.multilogin.core.impl.ISender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeSender implements ISender {
    private final CommandSender SENDER;

    public BungeeSender(CommandSender SENDER) {
        this.SENDER = SENDER;
    }

    @Override
    public String getName() {
        return SENDER.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return SENDER.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        SENDER.sendMessage(new TextComponent(message));
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return SENDER instanceof ProxiedPlayer;
    }

    @Override
    public UUID getPlayerUniqueIdentifier() {
        return isPlayer() ? ((ProxiedPlayer) SENDER).getUniqueId() : null;
    }

    @Override
    public boolean kickPlayer(String message) {
        if (isPlayer()) {
            ((ProxiedPlayer) SENDER).disconnect(new TextComponent(message));
            return true;
        }
        return false;
    }
}
