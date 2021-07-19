package moe.caa.multilogin.core.command.commands.multilogin;

import moe.caa.multilogin.core.command.Permission;
import moe.caa.multilogin.core.command.SubCommand;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.IOException;

public class ReloadMultiLoginCommand extends SubCommand {

    protected ReloadMultiLoginCommand() {
        super("reload", Permission.MULTI_LOGIN_MULTI_LOGIN_RELOAD);
    }

    @Override
    public void execute(ISender sender, String[] args) throws IOException {
        MultiCore.reload();
        sender.sendMessage("已经成功重新加载");
    }
}
