package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.plugin.ISender;
import net.kyori.adventure.text.Component;

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
    public boolean hasPermission(String permission) {
        return commandSource.hasPermission(permission);
    }

    @Override
    public void sendMessage(String message) {
        commandSource.sendMessage(Component.text(message));
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
