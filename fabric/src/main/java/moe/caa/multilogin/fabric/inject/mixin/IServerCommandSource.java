package moe.caa.multilogin.fabric.inject.mixin;

import net.minecraft.server.command.CommandOutput;

public interface IServerCommandSource {
    CommandOutput mlHandler_getCommandOutput();
}
