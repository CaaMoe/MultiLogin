package moe.caa.multilogin.core.auth.verify.section;

import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthReasonEnum;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.AbstractUserLogin;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Objects;

/**
 * Yggdrasil 账户验证身份核查
 */
public class VerifyIdentity extends AbstractVerify {

    @Override
    public VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result, AbstractUserLogin userLogin) throws Exception {
        // TODO: 2021/9/14 DEBUG
        try {
            User user = verifyAuthCore.getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(result.getResult().getId());
            if (user == null) return new VerifyAuthResult(VerifyAuthReasonEnum.PASS, null);
            if (Objects.equals(result.getService().getPathString(), user.getYggdrasilService()))
                return new VerifyAuthResult(VerifyAuthReasonEnum.PASS, null);
            return new VerifyAuthResult(VerifyAuthReasonEnum.FAIL, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_yggdrasil_changed", FormatContent.empty()));
        } catch (Exception e) {
            throw e;
        }
    }
}
