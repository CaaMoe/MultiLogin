/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.auth.AuthResult
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.data.data.YggdrasilServiceEntry;

/**
 * 对Yggdrasil的验证结果
 *
 * @param <T> 所需的数据类型
 */
public class AuthResult<T> {
    private final AuthErrorEnum err;
    private final T result;
    private final YggdrasilServiceEntry yggdrasilService;
    private Throwable throwable;

    protected AuthResult(T result, YggdrasilServiceEntry yggdrasilService) {
        this.err = null;
        this.result = result;
        this.yggdrasilService = yggdrasilService;
        throwable = null;
    }

    protected AuthResult(AuthErrorEnum err, YggdrasilServiceEntry yggdrasilService) {
        this.err = err;
        this.result = null;
        this.yggdrasilService = yggdrasilService;
        throwable = null;
    }


    protected AuthResult(AuthErrorEnum err) {
        this.err = err;
        this.result = null;
        this.yggdrasilService = null;
        throwable = null;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    /**
     * 得到验证失败枚举
     *
     * @return 验证失败的枚举，当此值为NULL即为验证成功
     */
    public AuthErrorEnum getErr() {
        return err;
    }

    /**
     * 返回验证结果
     *
     * @return 验证结果，当getErr不为空，此值为空
     */
    public T getResult() {
        return result;
    }

    /**
     * 返回此次验证所关联的Yggdrasil服务器对象
     *
     * @return Yggdrasil服务器对象
     */
    public YggdrasilServiceEntry getYggdrasilService() {
        return yggdrasilService;
    }

    public boolean isSuccess() {
        return result != null && err == null && throwable == null;
    }
}

