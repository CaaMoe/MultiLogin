package moe.caa.multilogin.core.auth;

import lombok.var;
import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthCore;
import moe.caa.multilogin.core.impl.IUserLogin;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;

/**
 * 综合性验证核心<br>
 * 账户必须通过 yggdrasil 在线账户验证和 verify 安防核查才能正常登入游戏.
 *
 * @see moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthCore
 * @see moe.caa.multilogin.core.auth.verify.VerifyAuthCore
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
    public void doAuth(IUserLogin userLogin) throws SQLException, InterruptedException {
        var yggdrasilAuthResult = yggdrasilAuthCore.yggdrasilAuth(userLogin);
        // 处理 Yggdrasil failed
        var verifyAuthResult = verifyAuthCore.verifyAuth(yggdrasilAuthResult, userLogin);
        // 处理 Verify failed

    }
}
