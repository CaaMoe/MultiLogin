package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AllArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.impl.Callback;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.GroupBurstArrayList;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 代表前置验证核心类<br>
 * 服务端 HasJoined 阶段 Yggdrasil 账户验证阶段处理类
 */
@AllArgsConstructor
public class YggdrasilAuthCore {
    private final AtomicInteger authThreadId = new AtomicInteger(0);
    private final ExecutorService authExecutor = Executors.newScheduledThreadPool(5,
            r -> new Thread(r, "MultiLogin Authenticator #" + authThreadId.incrementAndGet()));
    private final MultiCore core;

    /**
     * 进行 HasJoined 验证
     *
     * @param user 用户数据
     */
    public YggdrasilAuthResult yggdrasilAuth(BaseUserLogin user) {
        try {
            // 服务器宕机
            AtomicBoolean down = new AtomicBoolean(false);
            // 放顺序的
            var order = getVerifyOrder(user.getUsername());
            if (order.size() == 0) return new YggdrasilAuthResult(YggdrasilAuthReasonEnum.NO_SERVICE, null, null);
            // 放验证成功后的结果的
            var succeed = new AtomicReference<YggdrasilAuthTask>();
            while (order.hasNext()) {
                var latch = new CountDownLatch(1);
                // 下一分组
                var crtService = order.next();
                // 放当前正在验证的线程的
                var currentAuthTasks = new LinkedList<YggdrasilAuthTask>();
                // 回调器
                var callback = (Callback<YggdrasilAuthTask>) (task) -> {
                    if (task.getThrowable() != null) {
                        down.set(true);
                        return;
                    }
                    if (task.getResponse() != null && task.getResponse().isSucceed()) {
                        // 有两个登入验证凭据?
                        // 不，这绝对不可能
                        // 除非是使用了重复的或是不安全的 Yggdrasil 账户验证服务器
                        synchronized (succeed) {
                            if (succeed.get() != null) {
                                core.getLogger().log(LoggerLevel.WARN, "Maybe you have one or more Yggdrasil servers with duplicate configurations?");
                                core.getLogger().log(LoggerLevel.WARN, String.format("Because %s has multiple login credentials.", task.getResponse().getName()));
                                core.getLogger().log(LoggerLevel.WARN, "DON'T IGNORE THIS WARNING.");
                                return;
                            }
                            succeed.set(task);
                        }
                        succeed.set(task);
                        // 有结果后立刻释放执行线程
                        latch.countDown();
                    }
                    currentAuthTasks.remove(task);
                    if (currentAuthTasks.isEmpty()) latch.countDown();
                };
                // 执行线程的
                for (YggdrasilService service : crtService) {
                    var authTask = new YggdrasilAuthTask(core, service, user, callback);
                    currentAuthTasks.add(authTask);
                    authExecutor.execute(authTask);
                }
                //阻塞
                if (!latch.await(core.getConfig().getServicesTimeOut(), TimeUnit.MILLISECONDS)) {
                    down.set(true);
                }
                YggdrasilAuthTask task = succeed.get();
                if (task == null) continue;
                // 直接返回
                return new YggdrasilAuthResult(YggdrasilAuthReasonEnum.RETURN, task.getResponse(), task.getService());
            }
            return new YggdrasilAuthResult(down.get() ? YggdrasilAuthReasonEnum.SERVER_DOWN : YggdrasilAuthReasonEnum.VALIDATION_FAILED, null, null);
        } catch (Exception e){
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while processing authentication .", e);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "user: " + user);
            return new YggdrasilAuthResult(YggdrasilAuthReasonEnum.ERROR, null, null);
        }
    }

    /**
     * 按照玩家名字分批次排序 Yggdrasil 验证服务器，批次中间使用空格隔开
     *
     * @param name name
     * @return 验证服务器排序的结果
     */
    private GroupBurstArrayList<YggdrasilService> getVerifyOrder(String name) throws SQLException {
        // 返回结果
        var ret = new GroupBurstArrayList<YggdrasilService>();
        // 查在数据库中缓存的验证服务器对象(开启的)
        var one = core.getSqlManager().getUserDataHandler().getYggdrasilServiceByCurrentName(name).stream().filter(YggdrasilService::isEnable).collect(Collectors.toSet());
        var temp = new ArrayList<YggdrasilService>();
        for (YggdrasilService service : one) {
            if (service == null) continue;
            temp.add(service);
        }
        // 添加到第一梯队
        ret.offer(temp);
        temp = new ArrayList<>();
        var serviceList = core.getYggdrasilServicesHandler().getEnabledServices();
        for (YggdrasilService service : serviceList) {
            if (service == null) continue;
            if (one.contains(service)) continue;
            temp.add(service);
        }
        ret.offer(temp);
        return ret;
    }
}
