package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.core.main.MultiCore;

public class AuthHandler implements AuthAPI {
    private final MultiCore core;

    public AuthHandler(MultiCore core) {
        this.core = core;
    }

    @Override
    public AuthResult auth(String username, String serverId, String ip) {
        return AuthResult.ofDisallowed("Unsupported");
    }
}
