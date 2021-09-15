package moe.caa.multilogin.core.auth.verify.section;

import lombok.var;
import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthReasonEnum;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.FormatContent;

/**
 * 白名单检查程序
 */
public class VerifyWhitelist extends AbstractVerify {

    @Override
    public VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result) {
        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Checking the player whitelist... (server: %s, username: %s)",
                result.getService().getPathString(), result.getResult().getName()
        ));
        try {
            User user = verifyAuthCore.getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(result.getResult().getId());
            var whitelist = false;

            if (user == null) {
                verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("New player, no whitelist record. (server: %s, username: %s)",
                        result.getService().getPathString(), result.getResult().getName()
                ));
            } else {
                whitelist = user.isWhitelist();
            }

            var openWhitelist = verifyAuthCore.getCore().isWhitelist() || result.getService().isWhitelist();
            if (!whitelist && openWhitelist) {
                if (!(verifyAuthCore.getCore().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(result.getResult().getName()) | verifyAuthCore.getCore().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(result.getResult().getId().toString()))) {
                    verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Part of the whitelist has been opened, but the player does not have a whitelist. (server: %s, username: %s)",
                            result.getService().getPathString(), result.getResult().getName()
                    ));
                    return new VerifyAuthResult(VerifyAuthReasonEnum.FAIL, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_whitelist_not_contain", FormatContent.empty()));
                }
            }
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.WARN, String.format("Player whitelist verification succeeded. (server: %s, username: %s)",
                    result.getService().getPathString(), result.getResult().getName()
            ));
            return new VerifyAuthResult(VerifyAuthReasonEnum.PASS);
        } catch (Exception e) {
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.ERROR, "player whitelist", e);
            return new VerifyAuthResult(VerifyAuthReasonEnum.ERROR, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_error", FormatContent.empty()));
        }
    }
}
