package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerChat;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerCommand;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.velocity.injector.handler.MultiInitialLoginSessionHandler;
import moe.caa.multilogin.velocity.injector.redirect.MultiEncryptionResponse;
import moe.caa.multilogin.velocity.injector.redirect.MultiPlayerChat;
import moe.caa.multilogin.velocity.injector.redirect.MultiPlayerCommand;
import moe.caa.multilogin.velocity.injector.redirect.MultiServerLogin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class VelocityInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MultiInitialLoginSessionHandler.init();
        MultiPlayerChat.init();
        MultiPlayerCommand.init();

        // auth
        redirect(StateRegistry.LOGIN.serverbound, EncryptionResponse.class, MultiEncryptionResponse.class, () -> new MultiEncryptionResponse(multiCoreAPI));
        redirect(StateRegistry.LOGIN.serverbound, ServerLogin.class, MultiServerLogin.class, () -> new MultiServerLogin(multiCoreAPI));

        // chat
        redirect(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat.class, MultiPlayerChat::new);
        redirect(StateRegistry.PLAY.serverbound, PlayerCommand.class, MultiPlayerCommand.class, MultiPlayerCommand::new);

    }

    private synchronized <T> void redirect(StateRegistry.PacketRegistry bound, Class<T> target, Class<? extends T> redirect, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //定义一些基础
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");
        Field stateRegistry$packetRegistry_packetIdToSupplierField = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetIdToSupplier");
        Field stateRegistry$packetRegistry_packetClassToId = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetClassToId");

        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);
        Method map$putMethod = Map.class.getMethod("put", Object.class, Object.class);

        stateRegistry$packetRegistry_versionsField.setAccessible(true);
        stateRegistry$packetRegistry_packetIdToSupplierField.setAccessible(true);
        stateRegistry$packetRegistry_packetClassToId.setAccessible(true);

        //获取PacketRegistry下的private final Map<ProtocolVersion, ProtocolRegistry> versions;
        Map<?, ?> versionsObject = (Map<?, ?>) stateRegistry$packetRegistry_versionsField.get(bound);
        for (Map.Entry<?, ?> entry : versionsObject.entrySet()) {
            // ProtocolRegistry
            Object protocolRegistry = entry.getValue();

            // input
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetIdToSupplierObject = (Map<?, ?>) stateRegistry$packetRegistry_packetIdToSupplierField.get(protocolRegistry);

            // 入口转换
            for (Map.Entry<?, ?> e : packetIdToSupplierObject.entrySet()) {
                MinecraftPacket minecraftPacketObject = (MinecraftPacket) ((Supplier<?>) e.getValue()).get();
                // 类匹配则进行替换
                if (minecraftPacketObject.getClass().equals(target)) {
                    map$entry$setValueMethod.invoke(e, supplierRedirect);
                }
            }

            // output
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetClassToIdObject = (Map<?, ?>) stateRegistry$packetRegistry_packetClassToId.get(protocolRegistry);

            // class int
            List<Map.Entry<?, ?>> needModify = new ArrayList<>();
            for (Map.Entry<?, ?> packetToId : packetClassToIdObject.entrySet()) {
                // 目标匹配
                if (packetToId.getKey().equals(target)) {
                    needModify.add(packetToId);
                }
            }

            for (Map.Entry<?, ?> e : needModify) {
                map$putMethod.invoke(packetClassToIdObject, redirect, e.getValue());
            }
        }
    }
}
