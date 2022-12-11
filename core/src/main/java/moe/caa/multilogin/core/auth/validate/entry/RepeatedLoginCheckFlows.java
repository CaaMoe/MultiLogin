package moe.caa.multilogin.core.auth.validate.entry;

import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

/**
 * 重复登录检查器
 */
public class RepeatedLoginCheckFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public RepeatedLoginCheckFlows(MultiCore core) {
        this.core = core;
    }

    @Override
    public Signal run(ValidateContext validateContext) {
        IPlayer player = core.getPlugin().getRunServer().getPlayerManager().getPlayer(validateContext.getInGameProfile().getId());
        if (player == null) {
            return Signal.PASSED;
        }
        if (!validateContext.getYggdrasilAuthenticationResult().getYggdrasilServiceConfig().isRefuseRepeatedLogin()) {
            player.kickPlayer(core.getLanguageHandler().getMessage("in_game_repeated_login"));
            validateContext.setNeedWait(true);
            return Signal.PASSED;
        }
        validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_repeat_login"));
        return Signal.TERMINATED;
    }
}
