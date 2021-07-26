/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.multilogin.query.Query_OnlineUuidCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.UUID;

public class Query_OnlineUuidCommand extends SubCommand {

    protected Query_OnlineUuidCommand() {
        super(null, true);
    }

    @Override
    public void subExecute(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            UUID uuid = ValueUtil.getUUIDOrNull(args[0]);
            if (uuid == null) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(args[0])));
                return;
            }
            User user = MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByOnlineUuid(uuid);
            if (user == null) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_ONLINE_UUID.getMessage(args[0])));
                return;
            }
            runTask(() -> {
                sender.sendMessage(QueryCommand.toMessage(user));
            });
        } else {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }
}
