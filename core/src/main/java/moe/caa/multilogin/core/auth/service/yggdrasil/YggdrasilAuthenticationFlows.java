package moe.caa.multilogin.core.auth.service.yggdrasil;

import moe.caa.multilogin.api.profile.GameProfile;
import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;
import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * 一个工作流，进行对 Yggd 的 hasJoined 访问
 */
public class YggdrasilAuthenticationFlows extends BaseFlows<HasJoinedContext> {
    private final MultiCore core;
    private final String username;
    private final String serverId;
    private final String ip;
    private final BaseYggdrasilServiceConfig config;

    protected YggdrasilAuthenticationFlows(MultiCore core, String username, String serverId, String ip, BaseYggdrasilServiceConfig config) {
        this.core = core;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.config = config;
    }

    // 进行验证
    public GameProfile call() throws Exception {
        String url = config.generateAuthURL(username, serverId, ip);

        if (config.getHttpRequestMethod() == BaseYggdrasilServiceConfig.HttpRequestMethod.GET) {
            return call0(config, new Request.Builder()
                    .get()
                    .url(url)
                    .header("User-Agent", core.getHttpRequestHeaderUserAgent())
                    .build());
        } else if (config.getHttpRequestMethod() == BaseYggdrasilServiceConfig.HttpRequestMethod.POST) {
            return call0(config, new Request.Builder()
                    .post(RequestBody.create(
                            config.generateAuthPostContent(username, serverId, ip).getBytes(StandardCharsets.UTF_8)
                    ))
                    .url(url)
                    .header("User-Agent", core.getHttpRequestHeaderUserAgent())
                    .header("Content-Type", "application/json")
                    .build());
        }
        throw new UnsupportedOperationException("HttpRequestMethod");
    }


    private GameProfile call0(BaseYggdrasilServiceConfig config, Request request) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(config.getRetry(), config.getRetryDelay()))
                .addInterceptor(new LoggingInterceptor())
                .writeTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout()))
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .proxy(config.getAuthProxy().getProxy())
                .proxyAuthenticator(config.getAuthProxy().getProxyAuthenticator())
                .build();
        Call call = client.newCall(request);
        try (Response execute = call.execute()) {
            return core.getGson().fromJson(Objects.requireNonNull(execute.body()).string(), GameProfile.class);
        }
    }

    @Override
    public Signal run(HasJoinedContext hasJoinedContext) {
        try {
            GameProfile call = call();
            if (call != null && call.getId() != null) {
                hasJoinedContext.getResponse().set(new Pair<>(call, (config)));
                return Signal.PASSED;
            }
            return Signal.TERMINATED;
        } catch (Throwable e) {
            hasJoinedContext.getServiceUnavailable().put(config, e);
            return Signal.TERMINATED;
        }
    }
}
