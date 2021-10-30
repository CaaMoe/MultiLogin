package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.core.impl.BaseScheduler;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FabricScheduler extends BaseScheduler {
    private static final ConcurrentHashMap<Runnable, Long> syncTicks = new ConcurrentHashMap<>();

    public static void tick() {
        Iterator<Map.Entry<Runnable, Long>> itr = syncTicks.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<Runnable, Long> entry = itr.next();
            if (entry.getValue() <= 0) {
                entry.getKey().run();
                itr.remove();
            } else {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }

    @Override
    public void runTask(Runnable run, long delay) {
        syncTicks.put(run, delay / 50);
    }

    @Override
    public void runTask(Runnable run) {
        syncTicks.put(run, 0L);
    }
}
