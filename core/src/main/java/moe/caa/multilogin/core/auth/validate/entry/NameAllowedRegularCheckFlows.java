package moe.caa.multilogin.core.auth.validate.entry;

import moe.caa.multilogin.api.internal.util.Pair;
import moe.caa.multilogin.api.internal.util.ValueUtil;
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
        String nameAllowedRegular = core.getPluginConfig().getNameAllowedRegular();
        if (ValueUtil.isEmpty(nameAllowedRegular)) {
            return Signal.PASSED;
        }
        if (!Pattern.matches(nameAllowedRegular, validateContext.getBaseServiceAuthenticationResult().getResponse().getName())) {
            validateContext.setDisallowMessage(core.getLanguageHandler().getMessage("auth_validate_failed_username_mismatch",
                    new Pair<>("name", validateContext.getBaseServiceAuthenticationResult().getResponse().getName()),
                    new Pair<>("regular", nameAllowedRegular)
            ));
            return Signal.TERMINATED;
        }
        return Signal.PASSED;
    }
}
