package moe.caa.multilogin.paper.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import moe.caa.multilogin.common.internal.command.CommandManager;
import moe.caa.multilogin.common.internal.main.MultiCore;
import net.kyori.adventure.text.Component;

public class PaperCommandManager extends CommandManager<CommandSourceStack> {

    public PaperCommandManager(MultiCore core) {
        super(core);
    }


    @Override
    public boolean hasPermission(CommandSourceStack stack, String permission) {
        return stack.getSender().hasPermission(permission);
    }

    @Override
    public void sendMessage(CommandSourceStack stack, Component component) {
        stack.getSender().sendMessage(component);
    }
}
