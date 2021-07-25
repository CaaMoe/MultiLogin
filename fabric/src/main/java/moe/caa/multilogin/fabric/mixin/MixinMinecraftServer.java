package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.impl.ScheduleManager;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow protected abstract void shutdown();

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void onInitPlugin(CallbackInfo ci){
        if (!new MultiLoginFabric((MinecraftDedicatedServer) (Object)this).init()) {
            shutdown();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci){
        ScheduleManager.tick();
    }
}
