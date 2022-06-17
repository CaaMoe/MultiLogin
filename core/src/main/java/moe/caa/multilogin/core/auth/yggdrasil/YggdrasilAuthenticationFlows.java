package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.api.auth.yggdrasil.response.HasJoinedResponse;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.yggdrasil.HttpRequestMethod;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;
import okhttp3.*;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class YggdrasilAuthenticationFlows extends BaseFlows<HasJoinedContext> {
    private final MultiCore core;
    private final String username;
    private final String serverId;
    private final String ip;
    private final int yggdrasilId;

    protected YggdrasilAuthenticationFlows(MultiCore core, String username, String serverId, String ip, int yggdrasilId) {
        this.core = core;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.yggdrasilId = yggdrasilId;
    }

    public HasJoinedResponse call() throws Exception {
        YggdrasilServiceConfig.HasJoinedConfig hasJoinedConfig = core.getPluginConfig().getYggdrasilServiceMap().get(yggdrasilId).getHasJoined();
        String ipContent = hasJoinedConfig.getIpContent();
        if (!ValueUtil.isEmpty(ipContent)) {
            ipContent = ValueUtil.transPapi(ipContent, new Pair<>("ip", ip == null ? "" : ip));
        }

        String url = ValueUtil.transPapi(hasJoinedConfig.getUrl(),
                new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                new Pair<>("ip", URLEncoder.encode(ipContent, StandardCharsets.UTF_8))
        );

        if (hasJoinedConfig.getMethod() == HttpRequestMethod.GET) {
            return call0(hasJoinedConfig, new Request.Builder()
                    .get()
                    .url(url)
                    .build());
        } else if (hasJoinedConfig.getMethod() == HttpRequestMethod.POST) {
            return call0(hasJoinedConfig, new Request.Builder()
                    .post(RequestBody.create(ValueUtil.transPapi(hasJoinedConfig.getPostContent(),
                            new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                            new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                            new Pair<>("ip", URLEncoder.encode(ipContent, StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8)))
                    .url(url)
                    .build());
        }
        throw new UnsupportedOperationException("HttpRequestMethod");
    }


    private HasJoinedResponse call0(YggdrasilServiceConfig.HasJoinedConfig config, Request request) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(config.getRetry(), config.getRetryDelay()))
                .writeTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout()))
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .callTimeout(Duration.ofMillis(config.getTimeout()))
                .proxy(new Proxy(config.getProxy().getType(), new InetSocketAddress(config.getProxy().getHostname(), config.getProxy().getPort())))
                .build();
        Call call = client.newCall(request);
        try (Response execute = call.execute()) {
            return core.getGson().fromJson(execute.body().string(), HasJoinedResponse.class);
        }
    }

    @Override
    public Signal run(HasJoinedContext hasJoinedContext) {
        try {
            HasJoinedResponse response = call();
            if (response != null && response.getId() != null) {
                hasJoinedContext.getResponse().set(new Pair<>(response, yggdrasilId));
                return Signal.PASSED;
            }
            return Signal.TERMINATED;
        } catch (Throwable e) {
            hasJoinedContext.getServiceUnavailable().put(yggdrasilId, e);
            return Signal.TERMINATED;
        }
    }
}
