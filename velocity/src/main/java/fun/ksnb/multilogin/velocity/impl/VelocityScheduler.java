package fun.ksnb.multilogin.velocity.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;

public class VelocityScheduler extends BaseScheduler {
    @Override
    public void runTask(Runnable run, long delay) {
        runTaskAsync(run, delay);
    }

    @Override
    public void runTask(Runnable run) {
        runTaskAsync(run);
    }
}
