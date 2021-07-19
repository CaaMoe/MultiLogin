/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.multilogin.MultiLoginCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.commands.multilogin.independent.ReloadCommand;
import moe.caa.multilogin.core.command.commands.multilogin.query.QueryCommand;
import moe.caa.multilogin.core.command.impl.RootCommand;

public class MultiLoginCommand extends RootCommand {
    public MultiLoginCommand() {
        super(null);
        subCommands.put("reload", new ReloadCommand());
        subCommands.put("query", new QueryCommand());
    }
}
