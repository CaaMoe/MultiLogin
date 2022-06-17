package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class YggdrasilAuthenticationFlows extends BaseFlows<HasJoinedContext> {
    private final MultiCore core;
    private final String username;
    private final String serverId;
    private final String ip;
    private final int yggdrasilId;

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
            return call0(hasJoinedConfig.getRetry(), hasJoinedConfig.getRetryDelay(), hasJoinedConfig.getTimeout(), new Request.Builder()
                    .get()
                    .url(url)
                    .build());
        } else if (hasJoinedConfig.getMethod() == HttpRequestMethod.POST) {
            return call0(hasJoinedConfig.getRetry(), hasJoinedConfig.getRetryDelay(), hasJoinedConfig.getTimeout(), new Request.Builder()
                    .post(RequestBody.create(ValueUtil.transPapi(hasJoinedConfig.getPostContent(),
                            new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                            new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                            new Pair<>("ip", URLEncoder.encode(ipContent, StandardCharsets.UTF_8))).getBytes(StandardCharsets.UTF_8)))
                    .url(url)
                    .build());
        }
        throw new UnsupportedOperationException("HttpRequestMethod");
    }


    private HasJoinedResponse call0(int retry, int delay, int timeout, Request request) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(retry, delay))
                .writeTimeout(Duration.ofMillis(timeout))
                .readTimeout(Duration.ofMillis(timeout))
                .connectTimeout(Duration.ofMillis(timeout))
                .callTimeout(Duration.ofMillis(timeout))
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
