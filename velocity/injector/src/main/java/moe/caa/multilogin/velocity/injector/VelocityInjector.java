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

/**
 * Velocity 注入程序
 */
public class VelocityInjector implements Injector {

    @Override
    public void inject(MultiCoreAPI multiCoreAPI) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        MultiInitialLoginSessionHandler.init();
        MultiPlayerChat.init();
        MultiPlayerCommand.init();

        // auth
        redirectInput(StateRegistry.LOGIN.serverbound, EncryptionResponse.class, () -> new MultiEncryptionResponse(multiCoreAPI));
        redirectInput(StateRegistry.LOGIN.serverbound, ServerLogin.class, () -> new MultiServerLogin(multiCoreAPI));

        // chat
        redirectInput(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat::new);
        redirectInput(StateRegistry.PLAY.serverbound, PlayerCommand.class, MultiPlayerCommand::new);

        appendOutput(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat.class);
        appendOutput(StateRegistry.PLAY.serverbound, PlayerCommand.class, MultiPlayerCommand.class);
    }

    /**
     * 重定向数据包
     * @param bound 数据包方向
     * @param originalClass 原始数据包类对象
     * @param supplierRedirect 重定向后的 Supplier
     */
    private synchronized <T> void redirectInput(StateRegistry.PacketRegistry bound, Class<T> originalClass, Supplier<? extends T> supplierRedirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //定义一些基础
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");
        Field stateRegistry$packetRegistry_packetIdToSupplierField = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetIdToSupplier");

        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);

        stateRegistry$packetRegistry_versionsField.setAccessible(true);
        stateRegistry$packetRegistry_packetIdToSupplierField.setAccessible(true);

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
                if (minecraftPacketObject.getClass().equals(originalClass)) {
                    map$entry$setValueMethod.invoke(e, supplierRedirect);
                }
            }
        }
    }

    /**
     * 追加注册出口包
     * @param bound 数据包方向
     * @param originalClass 原始数据包类对象
     * @param appendClass 追加的数据包类对象
     */
    private synchronized <T> void appendOutput(StateRegistry.PacketRegistry bound, Class<T> originalClass, Class<? extends T> appendClass) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //定义一些基础
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");
        Field stateRegistry$packetRegistry_packetClassToId = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetClassToId");

        Method map$putMethod = Map.class.getMethod("put", Object.class, Object.class);

        stateRegistry$packetRegistry_versionsField.setAccessible(true);
        stateRegistry$packetRegistry_packetClassToId.setAccessible(true);

        //获取PacketRegistry下的private final Map<ProtocolVersion, ProtocolRegistry> versions;
        Map<?, ?> versionsObject = (Map<?, ?>) stateRegistry$packetRegistry_versionsField.get(bound);
        for (Map.Entry<?, ?> entry : versionsObject.entrySet()) {
            // ProtocolRegistry
            Object protocolRegistry = entry.getValue();

            // output
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetClassToIdObject = (Map<?, ?>) stateRegistry$packetRegistry_packetClassToId.get(protocolRegistry);

            // class int
            List<Map.Entry<?, ?>> needModify = new ArrayList<>();
            for (Map.Entry<?, ?> packetToId : packetClassToIdObject.entrySet()) {
                // 目标匹配
                if (packetToId.getKey().equals(originalClass)) {
                    needModify.add(packetToId);
                }
            }

            for (Map.Entry<?, ?> e : needModify) {
                map$putMethod.invoke(packetClassToIdObject, appendClass, e.getValue());
            }
        }
    }
}
