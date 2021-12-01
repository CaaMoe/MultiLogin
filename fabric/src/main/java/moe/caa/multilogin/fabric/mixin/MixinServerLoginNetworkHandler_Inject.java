package moe.caa.multilogin.fabric.mixin;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.fabric.mixininject.IEnhance_ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class MixinServerLoginNetworkHandler_Inject implements IEnhance_ServerLoginNetworkHandler {

    @Mutable
    @Shadow
    private GameProfile profile;

    @Override
    public GameProfile multiLogin_getProfile() {
        return profile;
    }

    @Override
    public void multiLogin_setProfile(GameProfile gameProfile) {
        profile = gameProfile;
    }
}