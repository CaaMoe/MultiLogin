package moe.caa.multilogin.flows.workflows;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 表示一个工作流
 *
 * @param <CONTEXT> 加工上下文
 */
public abstract class BaseFlows<CONTEXT> {
    private static final AtomicInteger asyncThreadId = new AtomicInteger(0);

    @Getter(value = AccessLevel.PROTECTED)
    private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        final Thread thread = new Thread(r, "MultiLogin Flows #" + asyncThreadId.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    });

    public static synchronized void close() {
        if (executorService.isShutdown()) return;
        executorService.shutdown();
    }

    /**
     * 开始加工
     */
    public abstract Signal run(CONTEXT context);
}
