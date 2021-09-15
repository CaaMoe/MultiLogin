package moe.caa.multilogin.core.auth.verify.section;

import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthReasonEnum;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.IUserLogin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.Objects;

/**
 * Yggdrasil 账户验证身份核查
 */
public class VerifyIdentity extends AbstractVerify {

    @Override
    public VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result, IUserLogin userLogin) {
        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Checking the player identity... (server: %s, username: %s, serverId: %s, ip: %s)",
                result.getService().getPathString(), userLogin.getUsername(), userLogin.getServerId(), userLogin.getIp() == null ? "unknown" : userLogin.getIp()
        ));
        try {
            User user = verifyAuthCore.getCore().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(result.getResult().getId());
            if (user == null) {
                verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("New player, no identity record. (server: %s, username: %s, serverId: %s, ip: %s)",
                        result.getService().getPathString(), userLogin.getUsername(), userLogin.getServerId(), userLogin.getIp() == null ? "unknown" : userLogin.getIp()
                ));
                return new VerifyAuthResult(VerifyAuthReasonEnum.PASS);
            }
            if (Objects.equals(result.getService().getPathString(), user.getYggdrasilService())) {
                verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Player identity verification succeeded. (server: %s, username: %s, serverId: %s, ip: %s)",
                        result.getService().getPathString(), userLogin.getUsername(), userLogin.getServerId(), userLogin.getIp() == null ? "unknown" : userLogin.getIp()
                ));
                return new VerifyAuthResult(VerifyAuthReasonEnum.PASS);
            }
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.WARN, String.format("The identity verification failed, and the player changed its login method from %s to %s. (server: %s, username: %s, serverId: %s, ip: %s)",
                    user.getYggdrasilService(), result.getService().getPathString(), result.getService().getPathString(), userLogin.getUsername(), userLogin.getServerId(), userLogin.getIp() == null ? "unknown" : userLogin.getIp()
            ));
            return new VerifyAuthResult(VerifyAuthReasonEnum.FAIL, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_yggdrasil_changed", FormatContent.empty()));
        } catch (Exception e) {
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.ERROR, "player identity", e);
            return new VerifyAuthResult(VerifyAuthReasonEnum.ERROR, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_error", FormatContent.empty()));
        }
    }
}
