/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.multilogin.independent.PrintCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.multilogin.independent;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.yggdrasil.YggdrasilService;

public class PrintCommand extends SubCommand {
    public PrintCommand() {
        super(Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD, true);
    }

    @Override
    public void subExecute(ISender sender, String[] args) throws Throwable {
        if (args.length == 0) {
            for (YggdrasilService service : MultiCore.getInstance().getYggdrasilServicesHandler().getServices()) {
                MultiCore.log(LoggerLevel.INFO, service.getName());
                MultiCore.log(LoggerLevel.INFO, service.getBody().getUrl());
            }
        } else {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }
}
