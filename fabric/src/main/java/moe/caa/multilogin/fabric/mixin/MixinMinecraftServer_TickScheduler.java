package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.impl.FabricScheduler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer_TickScheduler {

    @Inject(method = "tickWorlds", at = @At("HEAD"))
    private void onTickWorld(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (shouldKeepTicking.getAsBoolean()) {
            FabricScheduler.tick();
        }
    }
}
