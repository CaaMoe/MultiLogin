package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.Data;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * HasJoined 验证上下文
 */
@Data
public class HasJoinedContext {
    private final String username;
    private final String serverId;
    private final String ip;

    // 存放成功的标志
    private final AtomicReference<Pair<GameProfile, Pair<Integer, YggdrasilServiceConfig>>> response = new AtomicReference<>();

    // 存放异常的
    private final Map<Integer, Throwable> serviceUnavailable = new ConcurrentHashMap<>();

    // 存放没有通过验证的
    private final Set<Integer> authenticationFailed = ConcurrentHashMap.newKeySet();

    protected HasJoinedContext(String username, String serverId, String ip) {
        this.username = username;
        this.serverId = serverId;
        this.ip = ip;
    }
}
