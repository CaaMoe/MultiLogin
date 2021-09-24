package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.Getter;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.impl.Callback;
import moe.caa.multilogin.core.impl.IUserLogin;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.HttpUtil;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 代表一个验证请求的线程
 */

public class YggdrasilAuthTask implements Runnable {

    @Getter
    private final YggdrasilService service;
    private final IUserLogin user;
    private final Callback<YggdrasilAuthTask> callback;
    private final AtomicBoolean isCancel = new AtomicBoolean(false);
    private final MultiCore core;
    private boolean done = false;
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
    public YggdrasilAuthTask(MultiCore core, YggdrasilService service, IUserLogin user, Callback<YggdrasilAuthTask> callback) {
        this.core = core;
        this.service = service;
        this.user = user;
        this.callback = callback;
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
        try {
            response = call();
        } catch (Throwable e) {
            throwable = e;
        } finally {
            done = true;
        }
        if (!isCancel.get()) callback.solve(this);
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
                        new URL(service.buildUrl(user.getUsername(), user.getServerId(), user.getIp())),
                        service.buildPostContent(user.getUsername(), user.getServerId(), user.getIp()),
                        "application/json",
                        (int) core.getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
        return MultiCore.getGson().fromJson(
                HttpUtil.httpGet(
                        new URL(service.buildUrl(user.getUsername(), user.getServerId(), user.getIp())),
                        (int) core.getServicesTimeOut(),
                        service.getAuthRetry()
                ),
                HasJoinedResponse.class
        );
    }
}
