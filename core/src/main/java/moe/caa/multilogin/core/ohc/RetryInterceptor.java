package moe.caa.multilogin.core.ohc;

import moe.caa.multilogin.api.logger.LoggerProvider;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RetryInterceptor implements Interceptor {
    private final int retry;
    private final int delay;

    public RetryInterceptor(int retry, int delay) {
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
                if (response.isSuccessful()) {
                    return response;
                }
            } catch (IOException e) {
                if (tc > retry) throw e;
            }

            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedRetryException(e);
            }
            tc++;
            LoggerProvider.getLogger().debug("--> Retry " + tc);
        }
    }
}
