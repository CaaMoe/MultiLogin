package moe.caa.multilogin.core.ohc;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Http 日志打印拦截器
 */
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
            long size = bf.size();
            if (size > 0) LoggerProvider.getLogger().debug(String.format("--> (%d bytes)", size));
        }

        long startNs = System.nanoTime();
        Response response;
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
            long size = buffer.size();
            if (size > 0) LoggerProvider.getLogger().debug(String.format("<-- (%d bytes)", size));
        }

        return response;
    }
}
