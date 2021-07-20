package moe.caa.multilogin.fabric.schedule;

import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.MinecraftServer;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class ScheduleManager extends AbstractScheduler {
    private static final Map<Runnable, Long> TICKS = new Hashtable<>();
    public static MinecraftServer server;

    static {
        TICKS.put(()->{
            new MultiLoginFabric(server).init();
        }, 0L);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        TICKS.put(run, delay);
    }

    @Override
    public void runTask(Runnable run) {
        TICKS.put(run, 0L);
    }

    public static void tick(MinecraftServer server){
        ScheduleManager.server = server;
        Iterator<Map.Entry<Runnable, Long>> itr = TICKS.entrySet().iterator();
        while (itr.hasNext()){
            Map.Entry<Runnable, Long> entry = itr.next();
            if(entry.getValue() <= 0){
                entry.getKey().run();
                itr.remove();
            } else {
                entry.setValue(entry.getValue() - 1);
            }
        }
    }
}
