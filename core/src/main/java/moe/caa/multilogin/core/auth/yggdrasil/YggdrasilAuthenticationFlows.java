package moe.caa.multilogin.core.auth.yggdrasil;

import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;
import okhttp3.*;

import java.io.IOException;
import java.net.URLEncoder;
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
    private final int yggdrasilId;

    protected YggdrasilAuthenticationFlows(MultiCore core, String username, String serverId, String ip, int yggdrasilId) {
        this.core = core;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.yggdrasilId = yggdrasilId;
    }

    // 进行验证
    public Pair<GameProfile, YggdrasilServiceConfig> call() throws Exception {
        YggdrasilServiceConfig config = core.getPluginConfig().getIdMap().get(yggdrasilId);
        String ipContent = "";
        if (config.isPassIp() && !ValueUtil.isEmpty(ip)) {
            ipContent = config.getHasJoined().getIpContent();
            if (!ValueUtil.isEmpty(ipContent)) {
                ipContent = ValueUtil.transPapi(ipContent, new Pair<>("ip", URLEncoder.encode(ip, StandardCharsets.UTF_8)));
            }
        }

        String url = ValueUtil.transPapi(config.getHasJoined().getUrl(),
                new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                new Pair<>("ip", ipContent)
        );

        if (config.getHasJoined().getMethod() == YggdrasilServiceConfig.HttpRequestMethod.GET) {
            return new Pair<>(call0(config, new Request.Builder()
                    .get()
                    .url(url)
                    .build()), config);
        } else if (config.getHasJoined().getMethod() == YggdrasilServiceConfig.HttpRequestMethod.POST) {
            return new Pair<>(call0(config, new Request.Builder()
                    .post(RequestBody.create(ValueUtil.transPapi(config.getHasJoined().getPostContent(),
                            new Pair<>("username", URLEncoder.encode(username, StandardCharsets.UTF_8)),
                            new Pair<>("serverId", URLEncoder.encode(serverId, StandardCharsets.UTF_8)),
                            new Pair<>("ip", ipContent)).getBytes(StandardCharsets.UTF_8)))
                    .url(url)
                    .build()), config);
        }
        throw new UnsupportedOperationException("HttpRequestMethod");
    }


    private GameProfile call0(YggdrasilServiceConfig config, Request request) throws IOException {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(config.getRetry(), config.getRetryDelay()))
                .addInterceptor(new LoggingInterceptor())
                .writeTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout()))
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .proxy(config.getProxy().getProxy())
                .proxyAuthenticator(config.getProxy().getProxyAuthenticator())
                .build();
        Call call = client.newCall(request);
        try (Response execute = call.execute()) {
            return core.getGson().fromJson(Objects.requireNonNull(execute.body()).string(), GameProfile.class);
        }
    }

    @Override
    public Signal run(HasJoinedContext hasJoinedContext) {
        try {
            Pair<GameProfile, YggdrasilServiceConfig> response = call();
            if (response.getValue1() != null && response.getValue1().getId() != null) {
                hasJoinedContext.getResponse().set(new Pair<>(response.getValue1(), new Pair<>(yggdrasilId, response.getValue2())));
                return Signal.PASSED;
            }
            return Signal.TERMINATED;
        } catch (Throwable e) {
            hasJoinedContext.getServiceUnavailable().put(yggdrasilId, e);
            return Signal.TERMINATED;
        }
    }
}
