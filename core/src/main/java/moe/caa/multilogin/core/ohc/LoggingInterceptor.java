package moe.caa.multilogin.core.ohc;

import moe.caa.multilogin.api.logger.LoggerProvider;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LoggingInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        LoggerProvider.getLogger().debug(String.format("--> %s %s", request.method(), request.url()));

        RequestBody requestBody = request.body();
        if (requestBody != null) {
            Buffer bf = new Buffer();
            requestBody.writeTo(bf);
            LoggerProvider.getLogger().debug(String.format("--> (%d bytes)", bf.size()));
        }

        long startNs = System.nanoTime();
        Response response = null;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            LoggerProvider.getLogger().debug("<-- HTTP FAILED", e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        LoggerProvider.getLogger().debug(String.format("<-- %s %s (%dms)", response.code(), response.request().url(), tookMs));
        ResponseBody body = response.body();
        if (body != null) {

            BufferedSource source = body.source();
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.getBuffer();
            LoggerProvider.getLogger().debug(String.format("<-- (%d bytes)", buffer.size()));
        }

        return response;
    }
}
