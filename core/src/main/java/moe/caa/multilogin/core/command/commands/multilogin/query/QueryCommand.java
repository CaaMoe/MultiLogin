package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.impl.RootCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.language.LanguageKeys;

public class QueryCommand extends RootCommand {

    public QueryCommand() {
        super(null);
        subCommands.put("name", new Query_NameCommand());
        subCommands.put("onlineuuid", new Query_OnlineUuidCommand());
    }

    protected static String toMessage(User user) {
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(user.currentName, user.onlineUuid.toString(), user.redirectUuid.toString(), user.service.name, user.service.path, user.whitelist);
    }
}
