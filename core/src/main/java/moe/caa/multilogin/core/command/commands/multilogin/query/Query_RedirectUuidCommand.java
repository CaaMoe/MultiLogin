package moe.caa.multilogin.core.command.commands.multilogin.query;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.impl.SubCommand;
import moe.caa.multilogin.core.data.User;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ValueUtil;

import java.util.List;
import java.util.UUID;

public class Query_RedirectUuidCommand extends SubCommand {

    protected Query_RedirectUuidCommand() {
        super(Permission.MULTI_LOGIN_MULTI_LOGIN_QUERY, true);
    }

    @Override
    public void subExecute(ISender sender, String[] args) throws Throwable {
        if (args.length == 1) {
            UUID uuid = ValueUtil.getUUIDOrNull(args[0]);
            if (uuid == null) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_NO_UUID.getMessage(args[0])));
                return;
            }
            List<User> users = MultiCore.getInstance().getSqlManager().getUserDataHandler().getUserEntryByRedirectUuid(uuid);
            if (users.size() == 0) {
                runTask(() -> sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN_REDIRECT_UUID.getMessage(args[0])));
                return;
            }
            runTask(() -> {
                for (User user : users) {
                    sender.sendMessage(QueryCommand.toMessage(user));
                }

            });
        } else {
            sender.sendMessage(LanguageKeys.COMMAND_UNKNOWN.getMessage());
        }
    }
}
