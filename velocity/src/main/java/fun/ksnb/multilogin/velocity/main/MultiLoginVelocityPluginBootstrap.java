package fun.ksnb.multilogin.velocity.main;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.StateRegistry;
import fun.ksnb.multilogin.velocity.impl.VelocityServer;
import fun.ksnb.multilogin.velocity.loader.main.MultiLoginVelocityLoader;
import fun.ksnb.multilogin.velocity.pccsh.IPccsh;
import io.netty.util.collection.IntObjectMap;
import lombok.Getter;
import lombok.SneakyThrows;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    public void initService() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, InvocationTargetException {
        final String pccshName = IPccsh.getPccshName();
        Class<?> multiLoginEncryptionResponseClass = Class.forName("fun.ksnb.multilogin.velocity.pccsh." + pccshName + ".MultiLoginEncryptionResponse");
        Class<?> velocityUserLoginClass = Class.forName("fun.ksnb.multilogin.velocity.pccsh." + pccshName + ".VelocityUserLogin");

        multiLoginEncryptionResponseClass.getMethod("init").invoke(null);
        velocityUserLoginClass.getMethod("init").invoke(null);
//        要替换的方向
        StateRegistry.PacketRegistry toReplace = StateRegistry.LOGIN.serverbound;

        Field field_versions = ReflectUtil.handleAccessible(StateRegistry.PacketRegistry.class.getDeclaredField("versions"), true);
//        获取注册Map
        Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry> map = (Map<ProtocolVersion, StateRegistry.PacketRegistry.ProtocolRegistry>) field_versions.get(toReplace);
        for (StateRegistry.PacketRegistry.ProtocolRegistry protocolRegistry : map.values()) {
//            获取packetIdToSupplier Map
            Field field_packetIdToSupplier = ReflectUtil.handleAccessible(StateRegistry.PacketRegistry.ProtocolRegistry.class.getDeclaredField("packetIdToSupplier"), true);
            IntObjectMap<Supplier<? extends MinecraftPacket>> packetIdToSupplier = (IntObjectMap<Supplier<? extends MinecraftPacket>>) field_packetIdToSupplier.get(protocolRegistry);
//            至此 替换完成
            packetIdToSupplier.put(0x01, new Supplier<>() {
                @Override
                @SneakyThrows
                public MinecraftPacket get() {
                    return (MinecraftPacket) multiLoginEncryptionResponseClass.getConstructor().newInstance();
                }
            });
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
