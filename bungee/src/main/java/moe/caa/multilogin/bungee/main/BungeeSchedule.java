/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.main.BungeeSchedule
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.main;

import moe.caa.multilogin.core.impl.Scheduler;

public class BungeeSchedule extends Scheduler {
    private final MultiLoginBungee PLUGIN;

    public BungeeSchedule(MultiLoginBungee plugin) {
        PLUGIN = plugin;
    }

    //Bungeecord不需要同步任务
    @Override
    public void runTask(Runnable run, long delay) {
        runTaskAsync(run, delay);
//        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTask(Runnable run) {
        runTaskAsync(run);
//        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, 0, TimeUnit.MILLISECONDS);
    }
}
