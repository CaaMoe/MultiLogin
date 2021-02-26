package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.core.data.YggdrasilServiceEntry;

public class AuthResult<T> {
    private final AuthErrorEnum err;
    private final T result;
    private final YggdrasilServiceEntry yggdrasilService;

    protected AuthResult(AuthErrorEnum err, T result, YggdrasilServiceEntry yggdrasilService) {
        this.err = err;
        this.result = result;
        this.yggdrasilService = yggdrasilService;
    }

    protected AuthResult(T result, YggdrasilServiceEntry yggdrasilService) {
        this.err = null;
        this.result = result;
        this.yggdrasilService = yggdrasilService;
    }

    protected AuthResult(AuthErrorEnum err) {
        this.err = err;
        this.result = null;
        this.yggdrasilService = null;
    }

    public AuthErrorEnum getErr() {
        return err;
    }

    public T getResult() {
        return result;
    }

    public YggdrasilServiceEntry getYggdrasilService() {
        return yggdrasilService;
    }

}

