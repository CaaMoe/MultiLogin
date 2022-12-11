package moe.caa.multilogin.core.auth.validate;

import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.validate.entry.*;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.SequenceFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.util.Arrays;

/**
 * 游戏内验证集中处理程序
 */
public class ValidateAuthenticationService {
    private final MultiCore core;
    private final SequenceFlows<ValidateContext> sequenceFlows;

    public ValidateAuthenticationService(MultiCore core) {
        this.core = core;
        // 注意 flows 顺序不能乱
        this.sequenceFlows = new SequenceFlows<>(Arrays.asList(
                // 处理玩家的游戏内 UUID 和保存数据
                new InitialInGameUUIDFlows(core),
                // 名称正则检查
                new NameAllowedRegularCheckFlows(core),
                // 重名检查和抢占名称检查和名字更新
                new DuplicateNamesCheckFlows(core),
                // 白名单检查
                new WhitelistCheckFlows(core),
                // 重复上线检查
                new RepeatedLoginCheckFlows(core)
        ));
    }

    /**
     * 开始游戏内验证
     */
    public ValidateAuthenticationResult checkIn(String username, String serverId, String ip,
                                                YggdrasilAuthenticationResult yggdrasilAuthenticationResult) {
        ValidateContext context = new ValidateContext(username, serverId, ip, yggdrasilAuthenticationResult);
        Signal run = sequenceFlows.run(context);
        if (run == Signal.PASSED) {
            if (context.isNeedWait()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LoggerProvider.getLogger().debug(e);
                }
            }
            return ValidateAuthenticationResult.ofAllowed(context.getInGameProfile());
        }
        return ValidateAuthenticationResult.ofDisallowed(context.getDisallowMessage());
    }
}
