package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.event.PlayerQuitEvent;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "remove", at = @At("RETURN"))
    private void onQuit(ServerPlayerEntity player, CallbackInfo ci) {
        PlayerQuitEvent.INSTANCE.invoker().accept(new PlayerQuitEvent.EventData(player));
    }
}