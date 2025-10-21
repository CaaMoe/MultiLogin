package moe.caa.multilogin.paper.internal.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import moe.caa.multilogin.common.internal.command.CommandManager;
import moe.caa.multilogin.common.internal.command.Sender;
import moe.caa.multilogin.common.internal.main.MultiCore;
import moe.caa.multilogin.paper.internal.online.PaperOnlinePlayer;
import org.bukkit.entity.Player;

public class PaperCommandManager extends CommandManager<CommandSourceStack> {

    public PaperCommandManager(MultiCore core) {
        super(core);
    }

    @Override
    public Sender wrapSender(CommandSourceStack stack) {
        if (stack.getSender() instanceof Player player) {
            return new PaperOnlinePlayer(player);
        }
        return new PaperSender<>(stack.getSender());
    }
}
