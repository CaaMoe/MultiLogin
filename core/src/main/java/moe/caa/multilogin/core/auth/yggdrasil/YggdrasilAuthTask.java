package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.impl.Callback;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.net.URL;
import java.net.URLEncoder;

/**
 * 代表一个验证请求的线程
 */
public class YggdrasilAuthTask implements Runnable {

    @Getter
    private final YggdrasilService service;
    private final BaseUserLogin user;
    private final Callback<YggdrasilAuthTask> callback;
    private final MultiCore core;
    @Getter
    private HasJoinedResponse response;

    @Getter
    private Throwable throwable;

    /**
     * 构建这个验证方法
     *
     * @param core     插件核心
     * @param service  Yggdrasil 账户验证服务器实例
     * @param user     用户验证数据
     * @param callback 回调
     */
    public YggdrasilAuthTask(MultiCore core, YggdrasilService service, BaseUserLogin user, Callback<YggdrasilAuthTask> callback) {
        this.core = core;
        this.service = service;
        this.user = user;
        this.callback = callback;
    }

    /**
     * 开始进行网络验证
     */
    @Override
    public void run() {
        try {
            response = call();
        } catch (Throwable e) {
            throwable = e;
        } finally {
            callback.solve(this);
        }
    }

    /**
     * 开始进行网络验证
     *
     * @return 验证结果
     * @throws Exception 网络异常或其他异常
     */
    private HasJoinedResponse call() throws Exception {
        if (service.isPostMode()) return MultiCore.getGson().fromJson(
                HttpUtil.httpPostJson(
                        new URL(service.buildUrl(user.getUsername(), user.getServerId(), user.getIp())),
                        service.buildPostContent(user.getUsername(), user.getServerId(), user.getIp()),
                        "application/json",
                        (int) core.getConfig().getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
        return MultiCore.getGson().fromJson(
                HttpUtil.httpGet(
                        new URL(service.buildUrl(URLEncoder.encode(user.getUsername(), "UTF-8"), user.getServerId(), user.getIp())),
                        (int) core.getConfig().getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
    }
}
