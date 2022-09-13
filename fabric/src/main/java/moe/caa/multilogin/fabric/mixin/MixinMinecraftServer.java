package moe.caa.multilogin.fabric.mixin;

import moe.caa.multilogin.fabric.inject.mixin.IMinecraftServer_MLA;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

/**
 * 添加接口方法方便设置 ApiServices
 */
@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IMinecraftServer_MLA {

    @Mutable
    @Shadow
    @Final
    protected ApiServices apiServices;

    @Override
    public ApiServices mlHandler_getApiServices() {
        return apiServices;
    }

    @Override
    public void mlHandler_setApiServices(ApiServices apiServices) {
        this.apiServices = apiServices;
    }
}
