/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.impl.BukkitSender
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.ISender;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitSender implements ISender {
    private final CommandSender vanHandle;

    public BukkitSender(CommandSender vanHandle) {
        this.vanHandle = vanHandle;
    }

    @Override
    public String getSenderName() {
        return vanHandle.getName();
    }

    @Override
    public boolean hasPermission(String permission) {
        return vanHandle.hasPermission(permission);
    }

    @Override
    public void sendMessage(BaseComponent text) {
        vanHandle.spigot().sendMessage(text);
    }

    @Override
    public boolean isOp() {
        return vanHandle.isOp();
    }

    @Override
    public boolean isPlayer() {
        return vanHandle instanceof Player;
    }
}
