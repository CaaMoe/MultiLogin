/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.whitelist.DelWhitelistCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.data.database.handler.CacheWhitelistDataHandler;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

public class Whitelist_RemoveCommand extends SubCommand {
    protected Whitelist_RemoveCommand() {
        super(Permission.MULTI_LOGIN_WHITELIST_REMOVE, true);
    }

    @Override
    public void execute(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            boolean result = CacheWhitelistDataHandler.removeCacheWhitelist(args[0]);
            if (result) {
                MultiCore.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(args[0])));
            } else {
                MultiCore.plugin.getSchedule().runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(args[0])));
            }
        } else {

        }
    }
}
