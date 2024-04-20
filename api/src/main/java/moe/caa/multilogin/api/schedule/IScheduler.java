package moe.caa.multilogin.api.schedule;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@ApiStatus.Internal
public sealed interface IScheduler {
    static IScheduler buildSimple() {
        return new SimpleScheduler();
    }

    void runTaskAsync(Runnable runnable);

    void runTaskAsync(Runnable runnable, long delay);

    void runTaskAsyncTimer(Runnable run, long delay, long period);

    ScheduledExecutorService getExecutor();

    void shutdown();

    final class SimpleScheduler implements IScheduler {
        private final AtomicInteger asyncThreadId = new AtomicInteger(0);
        private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(16, r -> {
            Thread thread = new Thread(r);
            thread.setName("MultiLogin Async #" + asyncThreadId.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        });

        @Override
        public void runTaskAsync(Runnable runnable) {
            asyncExecutor.execute(runnable);
        }

        @Override
        public void runTaskAsync(Runnable runnable, long delay) {
            asyncExecutor.schedule(runnable, delay, TimeUnit.MILLISECONDS);
        }

        @Override
        public void runTaskAsyncTimer(Runnable run, long delay, long period) {
            asyncExecutor.scheduleAtFixedRate(run, delay, period, TimeUnit.MILLISECONDS);
        }

        @Override
        public ScheduledExecutorService getExecutor() {
            return asyncExecutor;
        }

        @Override
        public synchronized void shutdown() {
            if (asyncExecutor.isShutdown()) return;
            asyncExecutor.shutdown();
        }
    }
}
