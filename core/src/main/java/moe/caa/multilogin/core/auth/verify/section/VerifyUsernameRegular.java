package moe.caa.multilogin.core.auth.verify.section;

import lombok.var;
import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthReasonEnum;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.regex.Pattern;

/**
 * 账户用户名正则检查程序
 */
public class VerifyUsernameRegular extends AbstractVerify {

    @Override
    public VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result) {
        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Checking the regularity of the player username... (server: %s, username: %s)",
                result.getService().getPathString(), result.getResult().getName()
        ));
        var yggService = result.getService();
        var regular = yggService.getNameAllowedRegularString();
        if (ValueUtil.isEmpty(regular)) regular = verifyAuthCore.getCore().getNameAllowedRegular();
        if (Pattern.matches(regular, result.getResult().getName())) {
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("The regular verification of the player username is successful. (regular: %s, server: %s, username: %s)",
                    regular, result.getService().getPathString(), result.getResult().getName()
            ));
            return new VerifyAuthResult(VerifyAuthReasonEnum.PASS);
        }
        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("The regular verification of the player username failed. (regular: %s, server: %s, username: %s)",
                regular, result.getService().getPathString(), result.getResult().getName()
        ));
        return new VerifyAuthResult(VerifyAuthReasonEnum.ERROR, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_username_regular_mismatch", FormatContent.createContent(
                FormatContent.FormatEntry.builder().name("regular").content(regular).build()
        )));
    }
}
