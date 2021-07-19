package moe.caa.multilogin.bungee;

import moe.caa.multilogin.core.impl.ISchedule;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class BungeeSchedule implements ISchedule {
    public final Timer TIMER = new Timer();
    private final MultiLoginBungee PLUGIN;

    public BungeeSchedule(MultiLoginBungee plugin) {
        PLUGIN = plugin;
    }


    @Override
    public void runTaskAsync(Runnable run) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, 0);
    }

    @Override
    public void runTaskAsync(Runnable run, long delay) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, delay * 50);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long per) {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                run.run();
            }
        }, delay * 50, per * 50);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTask(Runnable run) {
        PLUGIN.getProxy().getScheduler().schedule(PLUGIN, run, 0, TimeUnit.MILLISECONDS);
    }
}
