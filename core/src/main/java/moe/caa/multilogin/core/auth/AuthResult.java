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


    protected AuthResult(AuthFailedEnum err) {
        this.err = err;
        this.result = null;
        this.service = null;
        throwable = null;
    }

    public boolean isSuccess() {
        return result != null && err == null && throwable == null;
    }
}
