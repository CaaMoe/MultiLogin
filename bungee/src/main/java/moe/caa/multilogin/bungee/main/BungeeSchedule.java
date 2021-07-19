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

import moe.caa.multilogin.core.impl.ISchedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BungeeSchedule implements ISchedule {
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);
    private final MultiLoginBungee PLUGIN;

    public BungeeSchedule(MultiLoginBungee plugin) {
        PLUGIN = plugin;
    }


    @Override
    public void runTaskAsync(Runnable run) {
        executor.submit(run);
    }

    @Override
    public void runTaskAsync(Runnable run, long delay) {
        executor.schedule(run, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long period) {
        executor.scheduleAtFixedRate(run, delay, period, TimeUnit.MILLISECONDS);
    }
//Bungeecord不需要同步任务
    @Override
    public void runTask(Runnable run, long delay) {
        runTaskAsync(run,delay);
//        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTask(Runnable run) {
        runTaskAsync(run);
//        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, 0, TimeUnit.MILLISECONDS);
    }
}
