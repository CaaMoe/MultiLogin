package moe.caa.multilogin.core.auth.verify.section;

import lombok.var;
import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthReasonEnum;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.FormatContent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 账户用户名重名检查
 */
public class VerifyDuplicateUsername extends AbstractVerify {

    @Override
    public VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result) {
        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Checking player duplicate username... (server: %s, username: %s)",
                result.getService().getPathString(), result.getResult().getName()
        ));
        try {
            if (!result.getService().isSafeId()) {
                var repeatedNameUserEntries = verifyAuthCore.getCore().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(result.getResult().getName());
                for (var otherUser : repeatedNameUserEntries) {
                    if (!otherUser.getOnlineUuid().equals(result.getResult().getId())) {
                        verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Contains conflicting username. (server: %s, username: %s)",
                                result.getService().getPathString(), result.getResult().getName()
                        ));
                        return new VerifyAuthResult(VerifyAuthReasonEnum.FAIL, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_username_occupy", FormatContent.empty()));
                    }
                }
            }
            var latch = new CountDownLatch(1);
            verifyAuthCore.getCore().getPlugin().getRunServer().getScheduler().runTask(() -> {
                var handle = false;
                for (ISender sender : verifyAuthCore.getCore().getPlugin().getRunServer().getPlayerManager().getPlayer(result.getResult().getName())) {
                    handle = true;
                    sender.kickPlayer(verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_username_compulsory_possession", FormatContent.empty()));
                }
                if (handle)
                    verifyAuthCore.getCore().getLogger().log(LoggerLevel.WARN, String.format("Force ID change. (main id: %s, server: %s, username: %s)",
                            result.getResult().getName(), result.getService().getPathString(), result.getResult().getName()
                    ));
                latch.countDown();
            });
            if (!latch.await(5000, TimeUnit.MILLISECONDS))
                verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("Waiting for the player to be kicked out, timeout. (server: %s, username: %s)",
                        result.getService().getPathString(), result.getResult().getName()
                ));
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.DEBUG, String.format("The player username duplicate check succeeded. (server: %s, username: %s)",
                    result.getService().getPathString(), result.getResult().getName()
            ));
            return new VerifyAuthResult(VerifyAuthReasonEnum.PASS);
        } catch (Exception e) {
            verifyAuthCore.getCore().getLogger().log(LoggerLevel.ERROR, "player duplicate username", e);
            return new VerifyAuthResult(VerifyAuthReasonEnum.ERROR, verifyAuthCore.getCore().getLanguageHandler().getMessage("verify_error", FormatContent.empty()));
        }
    }
}
