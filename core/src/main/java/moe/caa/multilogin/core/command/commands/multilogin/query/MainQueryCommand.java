package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.language.LanguageKeys;

public class MainQueryCommand extends SubCommand {

    public MainQueryCommand() {
        super("query", null);
        subCommands.add(new QueryNameCommand());
    }

    protected static String toMessage(User user){
        return LanguageKeys.COMMAND_QUERY_ENTRY.getMessage(user.currentName, user.onlineUuid.toString(), user.redirectUuid.toString(), user.service.name, user.service.path, user.whitelist);
    }
}
