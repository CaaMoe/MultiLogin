package moe.caa.multilogin.velocity.injector;

import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerChat;
import moe.caa.multilogin.api.injector.Injector;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.velocity.injector.proxy.PlayerChatInvocationHandler;
import moe.caa.multilogin.velocity.injector.redirect.MultiEncryptionResponse;
import moe.caa.multilogin.velocity.injector.redirect.MultiPlayerChat;
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
        PlayerChatInvocationHandler.init();
        redirect(StateRegistry.LOGIN.serverbound, EncryptionResponse.class, () -> new MultiEncryptionResponse(multiCoreAPI));
        redirect(StateRegistry.LOGIN.serverbound, ServerLogin.class, () -> new MultiServerLogin(multiCoreAPI));

//        Enhancer playerChatEnhancer = new Enhancer();
//
//        playerChatEnhancer.setSuperclass(PlayerChat.class);
//        playerChatEnhancer.setCallback(new PlayerChatInterceptor());
//        redirect(StateRegistry.PLAY.serverbound, PlayerChat.class, ()->
//                (MinecraftPacket) playerChatEnhancer.create()
//        );


        redirect(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat::new);
        redirectOut(StateRegistry.PLAY.serverbound, PlayerChat.class, MultiPlayerChat.class);

    }

    private void redirect(StateRegistry.PacketRegistry bound, Class<?> target, Supplier<? extends MinecraftPacket> redirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //定义一些基础
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");
        Field stateRegistry$packetRegistry_packetIdToSupplierField = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetIdToSupplier");
        // 不想看到泛型警告，反正就执行一次而已
        Method map$entry$setValueMethod = Map.Entry.class.getMethod("setValue", Object.class);

        stateRegistry$packetRegistry_versionsField.setAccessible(true);
        stateRegistry$packetRegistry_packetIdToSupplierField.setAccessible(true);

        //获取PacketRegistry下的private final Map<ProtocolVersion, ProtocolRegistry> versions;
        Map<?, ?> versionsObject = (Map<?, ?>) stateRegistry$packetRegistry_versionsField.get(bound);
        for (Map.Entry<?, ?> entry : versionsObject.entrySet()) {
//            遍历所有版本查找该类
            Object protocolRegistry = entry.getValue();

//            该Map为入口Id转class 部分包只需注册这个
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetIdToSupplierObject = (Map<?, ?>) stateRegistry$packetRegistry_packetIdToSupplierField.get(protocolRegistry);
            for (Map.Entry<?, ?> e : packetIdToSupplierObject.entrySet()) {
                MinecraftPacket minecraftPacketObject = (MinecraftPacket) ((Supplier<?>) e.getValue()).get();
//                类匹配则进行替换
                if (minecraftPacketObject.getClass().equals(target)) {
                    map$entry$setValueMethod.invoke(e, redirect);
                }
            }
        }
    }


    private void redirectOut(StateRegistry.PacketRegistry bound, Class<?> target, Class<?> redirect) throws NoSuchFieldException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        //定义一些基础
        Class<StateRegistry.PacketRegistry> stateRegistry$packetRegistryClass = StateRegistry.PacketRegistry.class;
        Class<StateRegistry.PacketRegistry.ProtocolRegistry> stateRegistry$packetRegistry$protocolRegistryClass = StateRegistry.PacketRegistry.ProtocolRegistry.class;
        Field stateRegistry$packetRegistry_versionsField = stateRegistry$packetRegistryClass.getDeclaredField("versions");

        stateRegistry$packetRegistry_versionsField.setAccessible(true);

        //获取PacketRegistry下的private final Map<ProtocolVersion, ProtocolRegistry> versions;
        Map<?, ?> versionsObject = (Map<?, ?>) stateRegistry$packetRegistry_versionsField.get(bound);
        for (Map.Entry<?, ?> entry : versionsObject.entrySet()) {
//            遍历所有版本查找该类
            Object protocolRegistry = entry.getValue();
//                必要的基础
            Field stateRegistry$packetRegistry_packetClassToId = stateRegistry$packetRegistry$protocolRegistryClass.getDeclaredField("packetClassToId");
            Method map$putMethod = Map.class.getMethod("put", Object.class, Object.class);

            stateRegistry$packetRegistry_packetClassToId.setAccessible(true);

            //                此Map为出口Map
            // IntObjectMap<Supplier<? extends MinecraftPacket>>
            Map<?, ?> packetClassToIdObject = (Map<?, ?>) stateRegistry$packetRegistry_packetClassToId.get(protocolRegistry);

            List<Map.Entry<?, ?>> entryList = new ArrayList<>();
            for (Map.Entry<?, ?> en : packetClassToIdObject.entrySet()) {
//                类匹配则进行替换
                if (en.getKey().equals(target)) {
//                        记录该Entry
                    entryList.add(en);
                }
            }
//                重新Set进去
            for (Map.Entry<?, ?> en : entryList) {
                map$putMethod.invoke(packetClassToIdObject, redirect, en.getValue());
            }

        }
    }
}
