package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.impl.FabricSender;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(method = "onPlayerConnect", at = @At("RETURN"))
    private void onPlayerJoin(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci){
        MultiLoginFabric.plugin.onJoin(new FabricSender(player.getCommandSource()));
    }

    @Inject(method = "remove", at = @At("RETURN"))
    private void onQuit(ServerPlayerEntity player, CallbackInfo ci){
        MultiLoginFabric.plugin.onQuit(player.getGameProfile().getId());
    }
}
