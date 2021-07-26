/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.command.commands.multilogin.query.QueryCommand
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.RootCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.language.LanguageKeys;

public class QueryCommand extends RootCommand {

    public QueryCommand() {
        super(Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY);
        subCommands.put("name", new Query_NameCommand());
        subCommands.put("onlineuuid", new Query_OnlineUuidCommand());
        subCommands.put("redirectuuid", new Query_RedirectUuidCommand());
    }

    protected static String toMessage(User user) {
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(user.getCurrentName(), user.getOnlineUuid().toString(), user.getRedirectUuid().toString(), user.getService().getName(), user.getService().getPath(), user.isWhitelist());
    }
}
