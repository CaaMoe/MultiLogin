/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.main.VelocitySender
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import moe.caa.multilogin.core.impl.ISender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class VelocitySender implements ISender {
    CommandSource commandSource;

    public VelocitySender(CommandSource commandSource) {
        this.commandSource = commandSource;
    }

    @Override
    public String getName() {
        if (isPlayer()) {
            return ((Player) commandSource).getUsername();
        }
        return "Console";
    }

    @Override
    public boolean hasPermission(String permission) {
        return commandSource.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        commandSource.sendMessage(Component.text(message));
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public boolean isPlayer() {
        return commandSource instanceof Player;
    }

    @Override
    public UUID getPlayerUniqueIdentifier() {
        if (isPlayer()) {
            return ((Player) commandSource).getUniqueId();
        }
        return null;
    }

    @Override
    public boolean kickPlayer(String message) {
        if (commandSource instanceof ConnectedPlayer) {
            ((ConnectedPlayer) commandSource).disconnect(Component.text(message));
            return true;
        }
        return false;
    }
}
