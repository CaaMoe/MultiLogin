package moe.caa.multilogin.core.auth.verify.section;

import moe.caa.multilogin.core.auth.verify.VerifyAuthCore;
import moe.caa.multilogin.core.auth.verify.VerifyAuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;
import moe.caa.multilogin.core.impl.AbstractUserLogin;

import java.sql.SQLException;

/**
 * 抽象身份核查器
 */
public abstract class AbstractVerify {

    /**
     * 开始核查登入请求
     *
     * @param verifyAuthCore 后置验证核心
     * @param result         yggdrasil 在线账户验证结果
     * @param userLogin      用户数据
     * @return 核查结果
     */
    public abstract VerifyAuthResult check(VerifyAuthCore verifyAuthCore, YggdrasilAuthResult result, AbstractUserLogin userLogin) throws SQLException, Exception;
}
