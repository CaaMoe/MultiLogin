package moe.caa.multilogin.fabric.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WhitelistCommand.class)
public class MixinWhitelistCommand_CommandRegistry {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void onRegister(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {

        ci.cancel();
    }
}
