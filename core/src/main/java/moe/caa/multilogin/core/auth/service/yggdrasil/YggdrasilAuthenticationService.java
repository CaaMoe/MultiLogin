package moe.caa.multilogin.core.auth.service.yggdrasil;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.api.internal.util.ValueUtil;
import moe.caa.multilogin.core.configuration.service.BaseServiceConfig;
import moe.caa.multilogin.core.configuration.service.yggdrasil.BaseYggdrasilServiceConfig;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.EntrustFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * HasJoined 集中处理程序
 */
public class YggdrasilAuthenticationService {
    private final MultiCore core;

    public YggdrasilAuthenticationService(MultiCore core) {
        this.core = core;
    }

    /**
     * 开始验证
     */
    public YggdrasilAuthenticationResult hasJoined(String username, String serverId, String ip) throws SQLException {
        final Set<Integer> ids = core.getPluginConfig().getServiceIdMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof BaseYggdrasilServiceConfig)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        if (ids.size() == 0) return YggdrasilAuthenticationResult.ofNoService();


        // 主要的验证服务器ID表
        // 在HasJoined验证时最先开始验证
        Set<Integer> primaries = new HashSet<>();

        // 如果只添加了一个验证服务器，那么就直接把它置为 primary
        // 否则就读数据库选出最近的验证服务器作为 primary
        if (ids.size() == 1) {
            primaries.add(ids.iterator().next());
        } else {
            // 首先获取数据库里面保存的他的 inGameUUID
            UUID inGameUUID = core.getSqlManager().getInGameProfileTable().getInGameUUIDIgnoreCase(username);

            // 如果获取到了它的 inGameUUID，就去获取 Yggdrasil ID
            if (inGameUUID != null) {
                // 可能有多个
                primaries.addAll(core.getSqlManager().getUserDataTable().getOnlineServiceIds(inGameUUID));
            }
        }

        // 次要的验证服务器ID表
        // 在HasJoined验证时最后开始验证
        Set<Integer> secondaries = ids.stream().filter(i -> !primaries.contains(i)).collect(Collectors.toSet());

        LoggerProvider.getLogger().debug(String.format(
                "%s's hasJoined verification order: [%s], [%s]", username,
                ValueUtil.join(", ", ", ", primaries),
                ValueUtil.join(", ", ", ", secondaries)
        ));

        boolean serverBreakdown = false;
        if (primaries.size() != 0) {
            YggdrasilAuthenticationResult result = hasJoined0(username, serverId, ip, primaries);
            if (result.getReason() == YggdrasilAuthenticationResult.Reason.ALLOWED) return result;
            if (result.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
                serverBreakdown = true;
            }
        }
        if (secondaries.size() != 0) {
            YggdrasilAuthenticationResult result = hasJoined0(username, serverId, ip, secondaries);
            if (result.getReason() == YggdrasilAuthenticationResult.Reason.ALLOWED) return result;
            if (result.getReason() == YggdrasilAuthenticationResult.Reason.SERVER_BREAKDOWN) {
                serverBreakdown = true;
            }
        }
        if (serverBreakdown) return YggdrasilAuthenticationResult.ofServerBreakdown();
        return YggdrasilAuthenticationResult.ofValidationFailed();
    }

    private YggdrasilAuthenticationResult hasJoined0(String username, String serverId, String ip, Set<Integer> ids) {
        Set<BaseYggdrasilServiceConfig> serviceConfigs = new HashSet<>();
        for (Integer id : ids) {
            BaseServiceConfig config = core.getPluginConfig().getServiceIdMap().get(id);
            if (config instanceof BaseYggdrasilServiceConfig) {
                serviceConfigs.add((BaseYggdrasilServiceConfig) config);
            }
        }
        EntrustFlows<HasJoinedContext> flows = new EntrustFlows<>(serviceConfigs.stream()
                .map(i -> new YggdrasilAuthenticationFlows(core, username, serverId, ip, i))
                .collect(Collectors.toList())
        );

        final HasJoinedContext context = new HasJoinedContext(username, serverId, ip);
        final Signal run = flows.run(context);
        if (run == Signal.PASSED) {
            return YggdrasilAuthenticationResult.ofAllowed(
                    context.getResponse().get().getValue1(),
                    context.getResponse().get().getValue2()
            );
        }
        if (context.getServiceUnavailable().size() != 0) {
            for (Map.Entry<BaseYggdrasilServiceConfig, Throwable> entry : context.getServiceUnavailable().entrySet()) {
                LoggerProvider.getLogger().debug("An exception occurred during authentication of the yggdrasil service whose ID is " + entry.getKey().getId(), entry.getValue());
            }
            return YggdrasilAuthenticationResult.ofServerBreakdown();
        }
        return YggdrasilAuthenticationResult.ofValidationFailed();
    }
}
