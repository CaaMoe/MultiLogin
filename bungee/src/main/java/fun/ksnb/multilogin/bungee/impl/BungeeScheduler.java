package fun.ksnb.multilogin.bungee.impl;

import moe.caa.multilogin.api.internal.plugin.BaseScheduler;

/**
 * Bungee 的调度器对象
 */
public class BungeeScheduler extends BaseScheduler {
    @Override
    public void runTask(Runnable run, long delay) {
        runTaskAsync(run, delay);
    }
}
