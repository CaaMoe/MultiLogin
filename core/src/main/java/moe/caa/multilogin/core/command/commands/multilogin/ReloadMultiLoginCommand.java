package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;

public class ReloadMultiLoginCommand extends SubCommand {

    protected ReloadMultiLoginCommand() {
        super("reload", Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD);
    }

    @Override
    public void execute(ISender sender, String[] args) throws Throwable {
        if(args.length == 0){
            MultiCore.reload();
            sender.sendMessage(LanguageKeys.COMMAND_RELOADED.getMessage());
        } else {
            super.execute(sender, args);
        }
    }
}
