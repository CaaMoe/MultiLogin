/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.whitelist.Whitelist_RemoveCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.whitelist;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.main.MultiCore;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class Whitelist_RemoveCommand extends SubCommand {
    protected Whitelist_RemoveCommand() {
        super(Permission.MULTI_LOGIN_WHITELIST_REMOVE, true);
    }

    @Override
    public void subExecute(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            boolean result = false;
            try {
                result = MultiCore.getInstance().getSqlManager().getCacheWhitelistDataHandler().removeCacheWhitelist(args[0]);
            } catch (SQLIntegrityConstraintViolationException throwables) {
                MultiCore.getInstance().getLogger().log(LoggerLevel.DEBUG, throwables);
            }
            if (result) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL.getMessage(args[0])));
            } else {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_WHITELIST_DEL_ALREADY.getMessage(args[0])));
            }
        } else {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }
}
