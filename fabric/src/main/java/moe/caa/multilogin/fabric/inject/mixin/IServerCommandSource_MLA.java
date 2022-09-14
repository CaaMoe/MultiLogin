package moe.caa.multilogin.fabric.inject.mixin;

import net.minecraft.server.command.CommandOutput;

/**
 * 丰富指令源方法
 */
public interface IServerCommandSource_MLA {
    CommandOutput mlHandler_getCommandOutput();
}
