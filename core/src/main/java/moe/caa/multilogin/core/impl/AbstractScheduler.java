/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.impl.Scheduler
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 代表一个调度器对象
 */
public abstract class AbstractScheduler {
    ScheduledExecutorService timerExecutor = Executors.newScheduledThreadPool(5);
    ExecutorService asyncExecutor = Executors.newCachedThreadPool();

    /**
     * 延迟执行任务
     *
     * @param run 任务
     */
    public void runTaskAsync(Runnable run) {
        asyncExecutor.execute(run);
    }

    /**
     * 异步延迟执行任务
     *
     * @param run   任务
     * @param delay 延迟
     */
    public void runTaskAsync(Runnable run, long delay) {
        timerExecutor.schedule(run, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 异步延迟执行周期任务
     *
     * @param run    任务
     * @param delay  延迟
     * @param period 周期
     */
    public void runTaskAsyncTimer(Runnable run, long delay, long period) {
        timerExecutor.scheduleAtFixedRate(run, delay, period, TimeUnit.MILLISECONDS);
    }

    /**
     * 延迟执行任务
     *
     * @param run   任务
     * @param delay 延迟
     */
    public abstract void runTask(Runnable run, long delay);

    /**
     * 执行任务
     *
     * @param run 任务
     */
    public abstract void runTask(Runnable run);
}
