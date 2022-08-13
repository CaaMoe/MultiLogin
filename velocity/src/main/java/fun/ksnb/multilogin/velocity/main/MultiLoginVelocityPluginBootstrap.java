package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerChat;
import com.velocitypowered.proxy.protocol.packet.chat.PlayerCommand;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import fun.ksnb.multilogin.velocity.loader.main.MultiLoginVelocityLoader;
import fun.ksnb.multilogin.velocity.main.v5t.redirect.MultiPlayerChat;
import fun.ksnb.multilogin.velocity.main.v5t.redirect.MultiPlayerCommand;
import fun.ksnb.multilogin.velocity.main.v5t.redirect.MultiServerLogin;
import fun.ksnb.multilogin.velocity.pccsh.MultiLoginEncryptionResponse;
import fun.ksnb.multilogin.velocity.pccsh.VelocityUserLogin;
import lombok.Getter;
import moe.caa.multilogin.core.exception.NoSuchEnumException;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiLoginVelocityPluginBootstrap extends BasePluginBootstrap implements IPlugin {
    @Getter
    private static MultiLoginVelocityPluginBootstrap instance;

    @Getter
    private final MultiCore core;
    private final MultiLoginVelocityLoader plugin;
    private IServer runServer;

    public MultiLoginVelocityPluginBootstrap(MultiLoginVelocityLoader plugin) {
        this.plugin = plugin;
        runServer = new VelocityServer(plugin.getServer());
        instance = this;
        core = new MultiCore(this);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = this;
        runServer = new VelocityServer(plugin.getServer());
        if (!core.init()) onDisable();
    }

    @Override
    public void onDisable() {
        core.disable();
    }

    @Override
    public void initService() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException, NoSuchEnumException {
        MultiLoginEncryptionResponse.init();
        VelocityUserLogin.init();
        MultiPlayerChat.init();
        MultiPlayerCommand.init();

        redirect(StateRegistry.LOGIN.serverbound, EncryptionResponse.class, MultiLoginEncryptionResponse.class, MultiLoginEncryptionResponse::new);
        redirect(StateRegistry.LOGIN.serverbound, ServerLogin.class, MultiServerLogin.class, MultiServerLogin::new);
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


    @Override
    public void initOther() {
        CommandManager commandManager = plugin.getServer().getCommandManager();
        MultiLoginCommand command = new MultiLoginCommand(this);
        commandManager.register(commandManager.metaBuilder("multilogin").build(), command);
        commandManager.register(commandManager.metaBuilder("whitelist").build(), command);
    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        if (level == LoggerLevel.ERROR) plugin.getLogger().error(message, throwable);
        else if (level == LoggerLevel.WARN) plugin.getLogger().warn(message, throwable);
        else if (level == LoggerLevel.INFO) plugin.getLogger().info(message, throwable);
        else if (level == LoggerLevel.DEBUG) {
        } else plugin.getLogger().info(message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return runServer;
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public String getPluginVersion() {
        return getServer().getPluginManager().getPlugin("multilogin").get()
                .getDescription().getVersion().get();
    }

    public ProxyServer getServer() {
        return plugin.getServer();
    }

}
