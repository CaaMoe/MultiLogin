package moe.caa.multilogin.fabric.impl;

import moe.caa.multilogin.core.impl.AbstractScheduler;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

public class ScheduleManager extends AbstractScheduler {
    private static final Map<Runnable, Long> TICKS = new Hashtable<>();

    @Override
    public void runTask(Runnable run, long delay) {
        TICKS.put(run, delay / 50);
    }

    @Override
    public void runTask(Runnable run) {
        TICKS.put(run, 0L);
    }

    public static void tick(){
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