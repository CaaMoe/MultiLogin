package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.inject.mixin.IServerCommandSource_MLA;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 此处注入为，添加获取命令输出源
 */
@Mixin(ServerCommandSource.class)
public abstract class MixinServerCommandSourceMLA implements IServerCommandSource_MLA {
    @Shadow
    @Final
    private CommandOutput output;

    @Override
    public CommandOutput mlHandler_getCommandOutput() {
        return output;
    }
}
