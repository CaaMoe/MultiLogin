package moe.caa.multilogin.common.internal.command.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import moe.caa.multilogin.common.internal.command.CMDSender;
import moe.caa.multilogin.common.internal.command.CommandManager;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class SubCommand<SENDER> {
    public final List<CommandDescription> commandDescriptions = new ArrayList<>();
    public final CommandManager<SENDER> manager;

    public SubCommand(CommandManager<SENDER> manager) {
        this.manager = manager;
    }

    public abstract void register(ArgumentBuilder<SENDER, ?> builder);

    protected void addCommandDescription(
            String label,
            String permission,
            Component description
    ) {
        commandDescriptions.add(new CommandDescription(label, permission, description));
    }

    protected LiteralArgumentBuilder<SENDER> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    protected Predicate<SENDER> predicateHasPermission(String permission) {
        return sender -> hasPermission(sender, permission);
    }

    protected boolean hasPermission(SENDER sender, String permission) {
        return manager.senderMap.apply(sender).hasPermission(permission);
    }

    protected void sendMessage(SENDER sender, Component component) {
        manager.senderMap.apply(sender).sendMessage(component);
    }

    protected void sendMessage(SENDER sender, Component... components) {
        CMDSender cmdSender = manager.senderMap.apply(sender);
        for (Component component : components) {
            cmdSender.sendMessage(component);
        }
    }


    public record CommandDescription(
            String label,
            String permission,
            Component description
    ) {
    }
}
