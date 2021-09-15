package moe.caa.multilogin.core.auth.verify;

import lombok.AllArgsConstructor;
import lombok.Getter;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.impl.IUserLogin;
import moe.caa.multilogin.core.main.MultiCore;

/**
 * 代表后置验证核心类<br>
 * 服务端用户登入校验阶段处理类
 */
@AllArgsConstructor
@Getter
public class VerifyAuthCore {
    private final MultiCore core;

    /**
     * 进行登入校验
     *
     * @param result 在线认证结果返回
     * @param user   用户数据
     */
    public VerifyAuthResult verifyAuth(YggdrasilAuthResult result, IUserLogin user) {
        return null;
    }
}
