package moe.caa.multilogin.core.ohc;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 延时重试拦截器
 */
public class RetryInterceptor implements Interceptor {
    private final int retry;
    private final long delay;

    public RetryInterceptor(int retry, long delay) {
        this.retry = retry;
        this.delay = delay;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Response response;
        int tc = 0;
        while (true) {
            try {
                response = chain.proceed(request);
                return response;
            } catch (IOException e) {
                LoggerProvider.getLogger().debug(tc + " retry failed.", e);
                if (tc >= retry) throw e;
            }

            try {
                TimeUnit.MILLISECONDS.sleep(delay);
            } catch (InterruptedException e) {
                throw new InterruptedRetryException(e);
            }
            tc++;
            LoggerProvider.getLogger().debug("--> " + tc + " retry.");
        }
    }
}
