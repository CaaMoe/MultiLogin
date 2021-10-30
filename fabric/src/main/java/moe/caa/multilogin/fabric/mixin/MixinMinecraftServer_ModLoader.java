package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.loader.main.MultiLoginFabricLoader;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer_ModLoader {
    private MultiLoginFabricLoader multiLoginFabricLoader;

    @Shadow private volatile boolean running;

    @Inject(method = "loadWorld", at = @At("RETURN"))
    private void onLoad(CallbackInfo ci){
        multiLoginFabricLoader = new MultiLoginFabricLoader((MinecraftServer) (Object) this);
        multiLoginFabricLoader.onLoad();
        if(running){
            multiLoginFabricLoader.onEnable();
        }
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    private void onStopping(CallbackInfo ci){
        if(multiLoginFabricLoader != null){
            multiLoginFabricLoader.onDisable();
        }
    }
}
