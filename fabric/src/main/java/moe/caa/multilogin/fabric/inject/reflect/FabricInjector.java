package moe.caa.multilogin.fabric.inject.reflect;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import lombok.Getter;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.fabric.event.LoginStateOnKeyEvent;
import moe.caa.multilogin.fabric.event.LoginStatePlayerDisconnectEvent;
import moe.caa.multilogin.fabric.inject.mixin.IMinecraftServer_MLA;
import moe.caa.multilogin.fabric.inject.mixin.IServerLoginNetworkHandler_MLA;
import moe.caa.multilogin.fabric.inject.reflect.data.ServerLoginNetworkData;
import moe.caa.multilogin.fabric.inject.reflect.proxy.MinecraftSessionServiceInvocationHandler;
import moe.caa.multilogin.fabric.inject.reflect.proxy.SignatureVerifierInvocationHandler;
import moe.caa.multilogin.fabric.main.MultiLoginFabric;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;

import java.lang.reflect.Proxy;

/**
 * Fabric 的注入程序
 */
@Getter
public class FabricInjector implements Injector {
    @Getter
    private static FabricInjector injector;
    private final ServerLoginNetworkData serverLoginNetworkData = new ServerLoginNetworkData();
    private MinecraftServer minecraftServer;
    private MultiCoreAPI api;

    @Override
    public void inject(MultiCoreAPI api) throws Throwable {
        this.api = api;
        injector = this;
        minecraftServer = ((MultiLoginFabric) api.getPlugin()).getServer();

        IMinecraftServer_MLA serverMla = (IMinecraftServer_MLA) minecraftServer;
        ApiServices services = serverMla.mlHandler_getApiServices();

        serverMla.mlHandler_setApiServices(new ApiServices(
                (MinecraftSessionService) Proxy.newProxyInstance(services.getClass().getClassLoader(),
                        new Class[]{MinecraftSessionService.class}, new MinecraftSessionServiceInvocationHandler(this, services.sessionService())),

                (SignatureVerifier) Proxy.newProxyInstance(services.getClass().getClassLoader(),
                        new Class[]{SignatureVerifier.class}, new SignatureVerifierInvocationHandler(services.serviceSignatureVerifier())),
                services.profileRepository(), services.userCache()
        ));

        LoginStateOnKeyEvent.INSTANCE.register(e -> serverLoginNetworkData.setSocketAddress(
                ((IServerLoginNetworkHandler_MLA) e.serverLoginNetworkHandler()).mlHandler_getGameProfile(),
                e.serverLoginNetworkHandler().connection.getAddress()
        ));

        LoginStatePlayerDisconnectEvent.INSTANCE.register(e -> {
            String message = serverLoginNetworkData.getDisconnectMessage(Thread.currentThread());
            if (message != null) {
                e.setDisconnectText(Text.of(message));
            }
        });
    }
}
