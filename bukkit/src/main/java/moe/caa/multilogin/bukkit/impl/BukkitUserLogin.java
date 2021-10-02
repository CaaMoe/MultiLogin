package moe.caa.multilogin.bukkit.impl;

import moe.caa.multilogin.core.impl.IUserLogin;

public class BukkitUserLogin implements IUserLogin {
    private final Object vanHandler;

    public BukkitUserLogin(Object vanHandler) {
        this.vanHandler = vanHandler;
    }

    @Override
    public void disconnect(String message) {

    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public String getIp() {
        return null;
    }

    @Override
    public void startEncrypting() {

    }

    @Override
    public void finish() {

    }
}
