package moe.caa.multilogin.core.auth.before;

import lombok.AllArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

import java.sql.SQLException;
import java.util.LinkedHashSet;
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
    private final MultiCore core;

    /**
     * 进行 HasJoined 验证
     *
     * @param name     用户名
     * @param serverId 服务器ID
     * @return 验证数据返回
     */
    public HasJoinedResponse yggAuth(String name, String serverId) throws SQLException, InterruptedException {
        return yggAuth(name, serverId, null);
    }

    /**
     * 进行 HasJoined 验证
     *
     * @param name     用户名
     * @param serverId 服务器ID
     * @param ip       可选 ip
     * @return 验证数据返回
     */
    public HasJoinedResponse yggAuth(String name, String serverId, String ip) throws SQLException {
        core.getLogger().log(LoggerLevel.DEBUG, String.format("Start verifying player login request. (username: %s, serverId: %s, ip: %s)",
                name, serverId, ip == null ? "unknown" : ip
        ));
        var yggOrder = getVerifyOrder(name);
        return null;
    }

    /**
     * 按照玩家名字分批次排序 Yggdrasil 验证服务器，批次中间使用空格隔开
     *
     * @param name name
     * @return 验证服务器排序的结果
     */
    private LinkedHashSet<YggdrasilService> getVerifyOrder(String name) throws SQLException {
        var ret = new LinkedHashSet<YggdrasilService>();
        for (YggdrasilService service : core.getSqlManager().getUserDataHandler().getYggdrasilServiceByCurrentName(name)) {
            if (!service.isEnable()) continue;
            ret.add(service);
        }
        ret.add(null);
        var serviceList = core.getYggdrasilServicesHandler().getYggdrasilServices();
        for (YggdrasilService service : serviceList) {
            if (ret.contains(service)) continue;
            if (!service.isEnable()) continue;
            ret.add(service);
        }
        return ret;
    }
}
