package moe.caa.multilogin.core.auth.before;

import lombok.AllArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 代表前置验证核心类<br>
 * 服务端 HasJoined 阶段 Yggdrasil 账户验证阶段处理类
 */
@AllArgsConstructor
public class YggAuthCore {
    private final AtomicInteger authThreadId = new AtomicInteger(0);
    private final ExecutorService authExecutor = Executors.newScheduledThreadPool(5,
            r -> new Thread(r, "MultiLogin Authenticator #" + authThreadId.incrementAndGet()));
    private final AtomicInteger nextAuth = new AtomicInteger(0);
    private final MultiCore core;

    /**
     * 获得下一验证ID
     *
     * @return 下一验证ID
     */
    private int nextAuthId() {
        return nextAuth.incrementAndGet();
    }

    /**
     * 进行 HasJoined 验证
     *
     * @param name     用户名
     * @param serverId 服务器ID
     * @param ip       可选 ip
     * @return 验证数据返回
     */
    public AuthResult yggAuth(String name, String serverId, String ip) throws SQLException, InterruptedException {
        core.getLogger().log(LoggerLevel.DEBUG, String.format("Start verifying player login request. (username: %s, serverId: %s, ip: %s)",
                name, serverId, ip == null ? "unknown" : ip
        ));
        // 放顺序的
        var yggOrder = getVerifyOrder(name);
        // 分组获取循环的，一般只循环两次
        while (yggOrder.hasNext()) {
            // 获得下一分组服务器集合
            var currentServices = yggOrder.next();
            // 放当前正在验证的线程的
            var currentAuthTasks = new LinkedList<AuthTask>();
            // 阻塞循环用的
            var latch = new CountDownLatch(1);
            // 这是回调器，处理验证结果用的
            var callback = (Callback<HasJoinedResponse>) (response, throwable) -> {
                if (response != null && throwable == null) {
                }
                // 移除已经完成的线程的
                currentAuthTasks.removeIf(AuthTask::isDone);
                // 当前分组已经验证完的就释放
                if (currentAuthTasks.isEmpty()) latch.countDown();
            };
            // 执行线程的
            for (YggdrasilService service : currentServices) {
                var authTask = new AuthTask(service, name, serverId, ip, callback);
                currentAuthTasks.add(authTask);
                authExecutor.execute(authTask);
            }
            // 阻塞用
            latch.await();
        }


        return null;
    }

    /**
     * 进行 HasJoined 验证
     *
     * @param name     用户名
     * @param serverId 服务器ID
     * @return 验证数据返回
     */
    public AuthResult yggAuth(String name, String serverId) throws SQLException, InterruptedException {
        return yggAuth(name, serverId, null);
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
