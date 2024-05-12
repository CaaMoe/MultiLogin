package moe.caa.multilogin.core.auth.validate;

import moe.caa.multilogin.api.internal.logger.LoggerProvider;
import moe.caa.multilogin.core.auth.service.BaseServiceAuthenticationResult;
import moe.caa.multilogin.core.auth.validate.entry.AssignInGameFlows;
import moe.caa.multilogin.core.auth.validate.entry.InitialLoginDataFlows;
import moe.caa.multilogin.core.auth.validate.entry.NameAllowedRegularCheckFlows;
import moe.caa.multilogin.core.auth.validate.entry.WhitelistCheckFlows;
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
                // 登录记录
                new InitialLoginDataFlows(core),
                // 名称正则检查
                new NameAllowedRegularCheckFlows(core),
                // 白名单检查
                new WhitelistCheckFlows(core),
                // 处理玩家的游戏内 UUID 和分配
                new AssignInGameFlows(core)
        ));
    }

    /**
     * 开始游戏内验证
     */
    public ValidateAuthenticationResult checkIn(BaseServiceAuthenticationResult baseServiceAuthenticationResult) {
        ValidateContext context = new ValidateContext(baseServiceAuthenticationResult);
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
