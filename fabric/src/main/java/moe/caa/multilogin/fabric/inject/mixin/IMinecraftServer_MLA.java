package moe.caa.multilogin.fabric.inject.mixin;

import net.minecraft.util.ApiServices;

/**
 * 丰富指令源方法
 */
public interface IMinecraftServer_MLA {
    ApiServices mlHandler_getApiServices();

    void mlHandler_setApiServices(ApiServices apiServices);
}
