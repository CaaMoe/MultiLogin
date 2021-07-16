package moe.caa.multilogin.core.impl;

/**
 * 代表一个调度器对象
 */
public interface ISchedule {

    /**
     * 延迟执行任务
     *
     * @param run 任务
     */
    void runTaskAsync(Runnable run);

    /**
     * 异步延迟执行任务
     *
     * @param run   任务
     * @param delay 延迟
     */
    void runTaskAsync(Runnable run, long delay);

    /**
     * 异步延迟执行周期任务
     *
     * @param run   任务
     * @param delay 延迟
     * @param per   周期
     */
    void runTaskAsyncTimer(Runnable run, long delay, long per);

    /**
     * 延迟执行任务
     *
     * @param run   任务
     * @param delay 延迟
     */
    void runTask(Runnable run, long delay);

    /**
     * 执行任务
     *
     * @param run 任务
     */
    void runTask(Runnable run);
}
