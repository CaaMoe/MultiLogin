/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bukkit.impl.BukkitSchedule
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.impl.AbstractScheduler;

public class BukkitSchedule extends AbstractScheduler {
    private final MultiLoginBukkit PLUGIN;

    public BukkitSchedule(MultiLoginBukkit plugin) {
        PLUGIN = plugin;
    }

    @Override
    public void runTask(Runnable run) {
        PLUGIN.getServer().getScheduler().runTask(PLUGIN, run);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        PLUGIN.getServer().getScheduler().runTaskLater(PLUGIN, run, delay / 50);
    }
}
