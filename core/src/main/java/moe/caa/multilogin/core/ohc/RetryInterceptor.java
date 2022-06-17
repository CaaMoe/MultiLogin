package moe.caa.multilogin.core.ohc;

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
        Response response = chain.proceed(request);
        int tc = 0;
        while (!response.isSuccessful() && tc < retry) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new InterruptedRetryException(e);
            }
            tc++;
            response = chain.proceed(request);
        }
        return response;
    }
}
