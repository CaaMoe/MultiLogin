package moe.caa.multilogin.core.auth;

import lombok.var;
import moe.caa.multilogin.core.auth.response.HasJoinedResponse;
import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthCore;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthReasonEnum;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.impl.BaseUserLogin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.user.User;
import moe.caa.multilogin.core.util.FormatContent;

/**
 * 综合性验证核心<br>
 * 账户必须通过 yggdrasil 在线账户验证和 verify 安防核查才能正常登入游戏.
 *
 * @see YggdrasilAuthCore
 */
public class CombineAuthCore {
    private final MultiCore core;
    private final YggdrasilAuthCore yggdrasilAuthCore;
    private final VerifyAuthCore verifyAuthCore;

    /**
     * 构建这个综合验证核心
     *
     * @param core 插件核心
     */
    public CombineAuthCore(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthCore = new YggdrasilAuthCore(core);
        this.verifyAuthCore = new VerifyAuthCore(core);
    }

    /**
     * 对该名玩家进行综合验证
     */
    public void doAuth(BaseUserLogin userLogin) {
        // 进行 Yggdrasil 在线验证
        var yggdrasilAuthResult = yggdrasilAuthCore.yggdrasilAuth(userLogin);
        // Yggdrasil 在线验证失败的处置

        // TODO: 2021/10/19 user.name != ygg result name 
        if (!yggdrasilAuthResult.isSuccess()) {
            if (yggdrasilAuthResult.getReason() == YggdrasilAuthReasonEnum.SERVER_DOWN) {
                userLogin.disconnect(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_server_down", FormatContent.empty()));
                return;
            }
            if (yggdrasilAuthResult.getReason() == YggdrasilAuthReasonEnum.VALIDATION_FAILED) {
                userLogin.disconnect(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_validation_failed", FormatContent.empty()));
                return;
            }
            if (yggdrasilAuthResult.getReason() == YggdrasilAuthReasonEnum.NO_SERVICE) {
                userLogin.disconnect(core.getLanguageHandler().getMessage("auth_yggdrasil_failed_no_server", FormatContent.empty()));
                return;
            }
            if (yggdrasilAuthResult.getReason() == YggdrasilAuthReasonEnum.ERROR) {
                userLogin.disconnect(core.getLanguageHandler().getMessage("auth_yggdrasil_error", FormatContent.empty()));
                return;
            }
            userLogin.disconnect(core.getLanguageHandler().getMessage("auth_yggdrasil_error_unknown", FormatContent.empty()));
            return;
        }
        // 进行后置验证
        var verifyAuthResult = verifyAuthCore.verifyAuth(yggdrasilAuthResult, userLogin);
        // 后置验证失败的处置
        if (verifyAuthResult.isFailed()) {
            userLogin.disconnect(verifyAuthResult.getKickMessage());
            return;
        }
        User user = verifyAuthResult.getUser();
        user.setService(yggdrasilAuthResult.getService());
        core.getLogger().log(LoggerLevel.INFO, String.format("Authentication passed, login allowed. (user: %s, in game uuid: %s, online uuid: %s, yggdrasil: %s)",
                user.getCurrentName(), user.getRedirectUuid(), user.getOnlineUuid(), user.getYggdrasilService()));
        HasJoinedResponse response = generateHasJoinedGameProfile(yggdrasilAuthResult, verifyAuthResult);
        // 这里进行异步皮肤修复操作
        core.getRestorerCore().doRestorer(yggdrasilAuthResult, response);
        // 分发给后端 55555
        userLogin.setUser(user);
        userLogin.finish(response);
    }

    public HasJoinedResponse generateHasJoinedGameProfile(YggdrasilAuthResult yggdrasilAuthResult, VerifyAuthResult verifyAuthResult) {
        HasJoinedResponse response = new HasJoinedResponse();
        response.setPropertyMap(yggdrasilAuthResult.getResult().getPropertyMap());
        response.setId(verifyAuthResult.getUser().getRedirectUuid());
        response.setName(verifyAuthResult.getUser().getCurrentName());
        return response;
    }
}
