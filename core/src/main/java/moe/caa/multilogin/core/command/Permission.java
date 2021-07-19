/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.Permission
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command;

import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;

//命令及其子权限
public enum Permission {
    MULTI_LOGIN_UPDATE("multilogin.update"),
    MULTI_LOGIN_MULTI_LOGIN_RELOAD("multilogin.multilogin.reload"),
    MULTI_LOGIN_WHITELIST_ADD("multilogin.whitelist.add"),
    MULTI_LOGIN_WHITELIST_REMOVE("multilogin.whitelist.remove"),
    MULTI_LOGIN_MULTI_LOGIN_QUERY("multilogin.multilogin.query");

    public final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    public boolean hasPermission(ISender sender) {
        return sender.hasPermission(permission) || sender.isOp();
    }

    public boolean hasPermissionAndFeedback(ISender sender) {
        boolean ret = hasPermission(sender);
        if (!ret) sender.sendMessage(LanguageKeys.COMMAND_NO_PERMISSION.getMessage());
        return ret;
    }
}
