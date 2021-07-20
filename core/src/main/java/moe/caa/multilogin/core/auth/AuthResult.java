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

import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

public class AuthResult<T> {
    public final AuthFailedEnum err;
    public final T result;
    public final YggdrasilService service;
    public Throwable throwable;

    protected AuthResult(T result, YggdrasilService service) {
        this.err = null;
        this.result = result;
        this.service = service;
        throwable = null;
    }

    protected AuthResult(AuthFailedEnum err, YggdrasilService service) {
        this.err = err;
        this.result = null;
        this.service = service;
        throwable = null;
    }


//    强制要求加异常 不加不行！
    protected AuthResult(AuthFailedEnum err,Throwable throwable) {
        this.err = err;
        this.result = null;
        this.service = null;
        this.throwable = throwable;
    }

    public boolean isSuccess() {
        return result != null && err == null && throwable == null;
    }
}
