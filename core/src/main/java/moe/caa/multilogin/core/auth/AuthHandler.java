package moe.caa.multilogin.core.auth;

import moe.caa.multilogin.api.auth.AuthAPI;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationResult;
import moe.caa.multilogin.core.auth.yggdrasil.YggdrasilAuthenticationService;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;

public class AuthHandler implements AuthAPI {
    private final MultiCore core;
    private final YggdrasilAuthenticationService yggdrasilAuthenticationService;


    public AuthHandler(MultiCore core) {
        this.core = core;
        this.yggdrasilAuthenticationService = new YggdrasilAuthenticationService(core);
    }

    @Override
    public AuthResult auth(String username, String serverId, String ip) {
        try {
            YggdrasilAuthenticationResult yggdrasilAuthenticationResult = yggdrasilAuthenticationService.hasJoined(username, serverId, ip);
            System.out.println(yggdrasilAuthenticationResult);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return AuthResult.ofDisallowed("Unsupported");
    }
}
