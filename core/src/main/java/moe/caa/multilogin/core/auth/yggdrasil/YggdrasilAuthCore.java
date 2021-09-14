package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AllArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.impl.Callback;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.GroupBurstArrayList;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
    public YggdrasilAuthResult yggdrasilAuth(UserData user) throws SQLException, InterruptedException {
        core.getLogger().log(LoggerLevel.DEBUG, String.format("Start verifying player login request. (username: %s, serverId: %s, ip: %s)",
                user.username, user.serverId, user.ip == null ? "unknown" : user.ip
        ));
        // 服务器宕机
        AtomicBoolean down = new AtomicBoolean(false);
        // 放顺序的
        var order = getVerifyOrder(user.username);
        if (order.size() == 0) return new YggdrasilAuthResult(YggdrasilAuthReasonEnum.NO_SERVICE, null, null, null);
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
                    // TODO: 2021/9/14 PRINT ERROR
                    return;
                }
                if (task.getResponse().isSucceed()) {
                    succeed.set(task);
                    // 有结果后立刻释放执行线程
                    latch.countDown();
                }
                currentAuthTasks.remove(task);
                if (currentAuthTasks.isEmpty()) latch.countDown();
            };
            // 执行线程的
            for (YggdrasilService service : crtService) {
                var authTask = new YggdrasilAuthTask(service, user, callback);
                currentAuthTasks.add(authTask);
                authExecutor.execute(authTask);
            }
            //阻塞
            latch.await();
            YggdrasilAuthTask task = succeed.get();
            if (task == null) continue;
            // 直接返回
            return new YggdrasilAuthResult(YggdrasilAuthReasonEnum.RETURN, task.getResponse(), task.getService(), null);
        }
        return new YggdrasilAuthResult(down.get() ? YggdrasilAuthReasonEnum.SERVER_DOWN : YggdrasilAuthReasonEnum.VALIDATION_FAILED, null, null, null);
    }

    /**
     * 按照玩家名字分批次排序 Yggdrasil 验证服务器，批次中间使用空格隔开
     *
     * @param name name
     * @return 验证服务器排序的结果
     */
    private GroupBurstArrayList<YggdrasilService> getVerifyOrder(String name) throws SQLException {
        var ret = new GroupBurstArrayList<YggdrasilService>();
        var one = core.getSqlManager().getUserDataHandler().getYggdrasilServiceByCurrentName(name);
        var temp = new ArrayList<YggdrasilService>();
        for (YggdrasilService service : one) {
            if (service == null) continue;
            if (!service.isEnable()) continue;
            temp.add(service);
        }
        ret.offer(temp);
        var two = new ArrayList<YggdrasilService>();
        var serviceList = core.getYggdrasilServicesHandler().getYggdrasilServices();
        for (YggdrasilService service : serviceList) {
            if (service == null) continue;
            if (one.contains(service)) continue;
            if (!service.isEnable()) continue;
            two.add(service);
        }
        ret.offer(two);
        return ret;
    }
}
