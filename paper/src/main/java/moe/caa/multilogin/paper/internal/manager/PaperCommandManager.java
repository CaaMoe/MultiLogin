package moe.caa.multilogin.paper.internal.manager;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import moe.caa.multilogin.common.internal.data.Sender;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.common.internal.manager.CommandManager;
import moe.caa.multilogin.paper.internal.sender.PaperSender;

public class PaperCommandManager extends CommandManager<CommandSourceStack> {

    public PaperCommandManager(MultiCore core) {
        super(core);
    }

    @Override
    public Sender wrapSender(CommandSourceStack stack) {
        return PaperSender.wrapSender(stack.getSender());
    }
}
