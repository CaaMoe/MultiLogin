package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.api.plugin.BaseScheduler;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fabric 的积分榜对象
 */
public class FabricScheduler extends BaseScheduler {
    private final List<Entry> entries = Collections.synchronizedList(new ArrayList<>());


    public FabricScheduler(){
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            long currentTimeMillis = System.currentTimeMillis();
            entries.removeIf(entry -> {
                boolean b = entry.invalidTimeMillis < currentTimeMillis;
                if(b){
                    entry.run();
                    return true;
                }
                return false;
            });
        });
    }

    @Override
    public void runTask(Runnable run, long delay) {
        entries.add(new Entry(run, System.currentTimeMillis() + delay));
    }

    @Override
    public void runTask(Runnable run) {
        entries.add(new Entry(run, System.currentTimeMillis()));
    }

    private static class Entry {
        private final Runnable runnable;
        private final long invalidTimeMillis;

        private Entry(Runnable runnable, long invalidTimeMillis) {
            this.runnable = runnable;
            this.invalidTimeMillis = invalidTimeMillis;
        }

        public void run() {
            runnable.run();
        }
    }
}
