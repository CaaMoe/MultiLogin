package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.velocity.injector.proxy.PlayerChatInvocationHandler;
import moe.caa.multilogin.velocity.injector.redirect.MultiEncryptionResponse;
import moe.caa.multilogin.velocity.injector.redirect.MultiServerLogin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

public class VelocityInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MultiInitialLoginSessionHandler.init();
        PlayerChatInvocationHandler.init();
        redirect(StateRegistry.LOGIN.serverbound, EncryptionResponse.class, () -> new MultiEncryptionResponse(multiCoreAPI));
        redirect(StateRegistry.LOGIN.serverbound, ServerLogin.class, () -> new MultiServerLogin(multiCoreAPI));

//        redirect(StateRegistry.PLAY.serverbound, PlayerChat.class, ()->
//                (MinecraftPacket) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
//                        new Class[]{MinecraftPacket.class}, new PlayerChatInvocationHandler(new PlayerChat()))
//        );

        // Unable to find id for packet of type %s in %s protocol %s
        //redirect(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat::new);
    }

    private void redirect(StateRegistry.PacketRegistry bound, Class<?> target, Supplier<? extends MinecraftPacket> redirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");
        Field stateRegistry$packetRegistry_packetIdToSupplierField = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetIdToSupplier");
        // 不想看到泛型警告，反正就执行一次而已
        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);

        stateRegistry$packetRegistry_versionsField.setAccessible(true);
        stateRegistry$packetRegistry_packetIdToSupplierField.setAccessible(true);

        // Map<ProtocolVersion, ProtocolRegistry>
        Map<?, ?> versionsObject = (Map<?, ?>) stateRegistry$packetRegistry_versionsField.get(bound);
        for (Map.Entry<?, ?> entry : versionsObject.entrySet()) {
            Object value = entry.getValue();
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetIdToSupplierObject = (Map<?, ?>) stateRegistry$packetRegistry_packetIdToSupplierField.get(value);
            for (Map.Entry<?, ?> e : packetIdToSupplierObject.entrySet()) {
                MinecraftPacket minecraftPacketObject = (MinecraftPacket) ((Supplier<?>) e.getValue()).get();
                if (minecraftPacketObject.getClass().equals(target)) {
                    map$entry$setValueMethod.invoke(e, redirect);
                }
            }
        }
    }
}
