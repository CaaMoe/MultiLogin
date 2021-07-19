package moe.caa.multilogin.bungee.proxy;

import net.md_5.bungee.connection.LoginResult;

public class MultiLoginSignLoginResult extends LoginResult {
//    继承BungeeCord登入结果 公开构造方法
    public MultiLoginSignLoginResult(LoginResult result) {
        super(result.getId(), result.getName(), result.getProperties());
    }
}