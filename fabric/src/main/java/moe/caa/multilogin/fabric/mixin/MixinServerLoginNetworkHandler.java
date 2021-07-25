package moe.caa.multilogin.fabric.mixin;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.fabric.inject.IServerLoginNetworkHandler;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLoginNetworkHandler.class)
public class MixinServerLoginNetworkHandler implements IServerLoginNetworkHandler {

    @Shadow private GameProfile profile;

    @Override
    public GameProfile getProfile() {
        return profile;
    }
}
