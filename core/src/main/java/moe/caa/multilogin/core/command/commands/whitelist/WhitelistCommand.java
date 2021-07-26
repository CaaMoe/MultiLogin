/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.whitelist.WhitelistCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.impl.RootCommand;

//whitelist父节点
public class WhitelistCommand extends RootCommand {
    public WhitelistCommand() {
        super(null);
        subCommands.put("add", new Whitelist_AddCommand());
        subCommands.put("remove", new Whitelist_RemoveCommand());
    }
}
