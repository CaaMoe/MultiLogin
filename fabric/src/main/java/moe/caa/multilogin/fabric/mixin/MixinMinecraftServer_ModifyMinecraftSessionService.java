package moe.caa.multilogin.fabric.mixin;

import com.mojang.authlib.minecraft.HttpMinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import moe.caa.multilogin.fabric.auth.MultiLoginYggdrasilMinecraftSessionService;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer_ModifyMinecraftSessionService {

    @Mutable
    @Shadow @Final private MinecraftSessionService sessionService;

    @Inject(method = "getSessionService", at = @At("HEAD"), cancellable = true)
    private void onInit(CallbackInfoReturnable<MinecraftSessionService> cir){

    }
}
