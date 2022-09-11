package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.event.PluginEnableEvent;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftDedicatedServer.class)
public abstract class MixinMinecraftDedicatedServer {

    @Inject(method = "setupServer",
            at = @At(value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Lnet/minecraft/server/dedicated/MinecraftDedicatedServer;loadWorld()V"))
    private void onEnablePlugin(CallbackInfoReturnable<Boolean> cir) {

        PluginEnableEvent.INSTANCE.invoker().enable(((MinecraftDedicatedServer) (Object) this));
    }
}
