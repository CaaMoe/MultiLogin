/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.multilogin.query.Query_NameCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

import java.util.List;

public class Query_NameCommand extends SubCommand {

    protected Query_NameCommand() {
        super(null, true);
    }

    @Override
    public void subExecute(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            List<User> users = MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByCurrentName(args[0]);
            if (users.size() == 0) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_NAME.getMessage(args[0])));

                return;
            }
            runTask(() -> {
                sender.sendMessage(LanguageKeys.COMMAND_QUERY_LIST.getMessage(users.size()));
                for (User user : users) {
                    sender.sendMessage(QueryCommand.toMessage(user));
                }
            });
        } else {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }
}
