/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.impl.Command
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.impl;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.Collections;
import java.util.List;

//抽象命令父类
public abstract class Command {
    private final Permission permission;

    protected Command(Permission permission) {
        this.permission = permission;
    }

    //    节省代码的简便工具 执行方法
    public static void runTask(Runnable runnable) {
        MultiCore.getScheduler().runTask(runnable);
    }

    //    节省代码的简便工具 异步执行方法
    public static void runTaskAsync(Runnable runnable) {
        MultiCore.getScheduler().runTaskAsync(runnable);
    }

    public boolean canExecute(ISender iSender) {
        if (permission == null) return true;
        return permission.hasPermission(iSender);
    }

    //执行
    public abstract void execute(ISender sender, String[] args) throws Throwable;

    //    Tab补全
    public List<String> tabComplete(ISender sender, String[] args) throws Throwable {
        return Collections.emptyList();
    }
}
