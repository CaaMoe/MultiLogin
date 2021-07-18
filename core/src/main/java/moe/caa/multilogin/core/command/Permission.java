package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;

public enum Permission {
    MULTI_LOGIN_UPDATE("multilogin.update");

    public final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(ISender sender){
        return sender.hasPermission(permission) || sender.isOp();
    }

    public boolean hasPermissionAndFeedback(ISender sender){
        boolean ret = hasPermission(sender);
        if(!ret) sender.sendMessage(LanguageKeys.COMMAND_NO_PERMISSION.getMessage());
        return ret;
    }
}
