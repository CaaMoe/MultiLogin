package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.console.VelocityConsole;
import moe.caa.multilogin.api.internal.plugin.IPlayer;
import moe.caa.multilogin.api.internal.plugin.ISender;
import net.kyori.adventure.text.Component;

/**
 * Velocity 指令执行者对象
 */
public class VelocitySender implements ISender {
    private final CommandSource commandSource;

    public VelocitySender(CommandSource commandSource) {
        this.commandSource = commandSource;
    }

    @Override
    public boolean isPlayer() {
        return commandSource instanceof Player;
    }

    @Override
    public boolean isConsole() {
        return commandSource instanceof VelocityConsole;
    }

    @Override
    public boolean hasPermission(String permission) {
        return commandSource.hasPermission(permission);
    }

    @Override
    public void sendMessagePL(String message) {
        for (String s : message.split("\\r?\\n")) {
            commandSource.sendMessage(Component.text(s));
        }
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public IPlayer getAsPlayer() {
        return new VelocityPlayer((Player) commandSource);
    }
}
