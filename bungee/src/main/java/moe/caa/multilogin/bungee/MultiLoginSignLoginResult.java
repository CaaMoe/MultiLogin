package moe.caa.multilogin.bungee;

import net.md_5.bungee.connection.LoginResult;

public class MultiLoginSignLoginResult extends LoginResult {
    public MultiLoginSignLoginResult(LoginResult result) {
        super(result.getId(), result.getName(), result.getProperties());
    }
}
