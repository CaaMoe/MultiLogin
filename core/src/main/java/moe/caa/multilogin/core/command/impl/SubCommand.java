/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.impl.SubCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.impl;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.impl.ISender;

//子节点 最底层的执行节点
public abstract class SubCommand extends Command {
    protected final boolean async;

    //    基础构造
    protected SubCommand(Permission permission, boolean async) {
        super(permission);
        this.async = async;

    }

    //执行
    @Override
    public void execute(ISender sender, String[] args) throws Throwable {
        CommandTask task = new CommandTask(sender, args);
        if (async) {
            runTaskAsync(task);
        } else {
            runTask(task);
        }
    }

    private class CommandTask implements Runnable {
        ISender sender;
        String[] args;

        public CommandTask(ISender sender, String[] args) {
            this.sender = sender;
            this.args = args;
        }

        @Override
        public void run() {
            try {
                execute(sender, args);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
