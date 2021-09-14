package moe.caa.multilogin.core.auth.verify.section;

import moe.caa.multilogin.core.auth.verify.VerifyResultEnum;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthResult;

/**
 * 抽象身份核查器
 */
public abstract class AbstractVerify {

    /**
     * 开始核查登入请求
     *
     * @param result yggdrasil 在线账户验证结果
     * @return 核查结果
     */
    public abstract VerifyResultEnum check(YggdrasilAuthResult result);


}
