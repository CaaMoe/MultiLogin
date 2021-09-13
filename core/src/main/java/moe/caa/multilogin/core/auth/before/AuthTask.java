package moe.caa.multilogin.core.auth.before;

import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.Callback;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表一个验证请求的线程
 */

public class AuthTask implements Runnable {
    private final YggdrasilService service;
    private final String username;
    private final String serverId;
    private final String ip;
    private final Callback<HasJoinedResponse> callback;
    private final AtomicBoolean isCancel = new AtomicBoolean(false);
    private boolean done = false;

    /**
     * 构建这个验证方法
     *
     * @param service  Yggdrasil 账户验证服务器实例
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       用户IP（可选）
     * @param callback 回调
     */
    public AuthTask(YggdrasilService service, String username, String serverId, String ip, Callback<HasJoinedResponse> callback) {
        this.service = service;
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
        this.callback = callback;
    }

    /**
     * 构建这个验证方法
     *
     * @param service  Yggdrasil 账户验证服务器实例
     * @param username 用户名
     * @param serverId 服务器ID
     * @param callback 回调
     */
    public AuthTask(YggdrasilService service, String username, String serverId, Callback<HasJoinedResponse> callback) {
        this.service = service;
        this.username = username;
        this.serverId = serverId;
        this.callback = callback;
        this.ip = null;
    }

    /**
     * 设置执行后是否回调
     *
     * @param cancel 是否回调
     */
    public void setCancel(boolean cancel) {
        this.isCancel.set(cancel);
    }

    /**
     * 开始进行网络验证
     */
    @Override
    public void run() {
        HasJoinedResponse response = null;
        Throwable throwable = null;
        try {
            response = call();
        } catch (Throwable e) {
            throwable = e;
        } finally {
            done = true;
        }
        if (!isCancel.get()) callback.solve(response, throwable);
    }

    /**
     * 判断任务是否已经完成
     *
     * @return 任务是否已经完成
     */
    public boolean isDone() {
        return done;
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
                        new URL(service.buildUrl(username, serverId, ip)),
                        service.buildPostContent(username, serverId, ip),
                        "application/json",
                        (int) MultiCore.getCore().getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
        return MultiCore.getGson().fromJson(
                HttpUtil.httpGet(
                        new URL(service.buildUrl(username, serverId, ip)),
                        (int) MultiCore.getCore().getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
    }
}
