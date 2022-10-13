package moe.caa.multilogin.core.auth.validate.entry;

import moe.caa.multilogin.api.util.Pair;
import moe.caa.multilogin.api.util.ValueUtil;
import moe.caa.multilogin.core.auth.validate.ValidateContext;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.flows.workflows.BaseFlows;
import moe.caa.multilogin.flows.workflows.Signal;

import java.util.regex.Pattern;

/**
 * 玩家名字正则检查器
 */
public class NameAllowedRegularCheckFlows extends BaseFlows<ValidateContext> {
    private final MultiCore core;

    public NameAllowedRegularCheckFlows(MultiCore core) {
        this.core = core;
    }

    @Override
    public Signal run(ValidateContext validateContext) {
        String nameAllowedRegular = validateContext.getYggdrasilAuthenticationResult().getYggdrasilServiceConfig().getNameAllowedRegular();
        if (ValueUtil.isEmpty(nameAllowedRegular)) {
            return Signal.PASSED;
        }
        if (!Pattern.matches(nameAllowedRegular, validateContext.getYggdrasilAuthenticationResult().getResponse().getName())) {
            validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_mismatch",
                    new Pair<>("current_username", validateContext.getYggdrasilAuthenticationResult().getResponse().getName()),
                    new Pair<>("name_allowed_regular", nameAllowedRegular)
            ));
            return Signal.TERMINATED;
        }
        return Signal.PASSED;
    }
}
