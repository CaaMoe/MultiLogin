package fun.ksnb.multilogin.bungee.main;

import fun.ksnb.multilogin.bungee.auth.MultiLoginEncryptionResponse;
import fun.ksnb.multilogin.bungee.impl.BungeeServer;
import fun.ksnb.multilogin.bungee.impl.BungeeUserLogin;
import gnu.trove.map.TIntObjectMap;
import lombok.Getter;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.IServer;
import moe.caa.multilogin.core.loader.impl.BasePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import java.util.logging.Level;

public class MultiLoginBungeePluginBootstrap extends BasePluginBootstrap implements IPlugin {
    @Getter
    private static MultiLoginBungeePluginBootstrap instance;

    @Getter
    private final MultiCore core;

    private final Plugin vanPlugin;
    private final ProxyServer vanServer;

    private IServer server;

    public MultiLoginBungeePluginBootstrap(Plugin vanPlugin, ProxyServer vanServer) {
        this.vanPlugin = vanPlugin;
        this.vanServer = vanServer;
        this.core = new MultiCore(this);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {
        instance = this;
        server = new BungeeServer(BungeeCord.getInstance());
        if (!core.init()) onDisable();
    }

    @Override
    public void onDisable() {
        core.disable();
    }

    @Override
    public void initService() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, UnsupportedException {
        BungeeUserLogin.init();
        MultiLoginEncryptionResponse.init();
        Class<?> protocol_directionDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol_protocolDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");


        Field field_protocols = ReflectUtil.handleAccessible(protocol_directionDataClass.getDeclaredField("protocols"), true);
        Field field_TO_SERVER = ReflectUtil.handleAccessible(Protocol.class.getDeclaredField("TO_SERVER"), true);
        Field field_packetConstructors = ReflectUtil.handleAccessible(protocol_protocolDataClass.getDeclaredField("packetConstructors"), true);
        Object to_server = field_TO_SERVER.get(Protocol.LOGIN);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) field_protocols.get(to_server);
        for (int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            if (protocol >= 47) {
                Object data = protocols.get(protocol);
                //2021/2/28 Fixed Supplier unsupported problem
                Object[] constructors = (Object[]) field_packetConstructors.get(data);
                if (constructors instanceof Supplier[]) {
                    Supplier<? extends DefinedPacket>[] suppliers = (Supplier<? extends DefinedPacket>[]) constructors;
                    suppliers[0x01] = (Supplier<DefinedPacket>) MultiLoginEncryptionResponse::new;
                } else if (constructors instanceof Constructor[]) {
                    constructors[0x01] = MultiLoginEncryptionResponse.class.getDeclaredConstructor();
                } else {
                    throw new UnsupportedException("Unsupported server.");
                }
            }
        }
    }

    @Override
    public void initOther() {
        vanServer.getPluginManager().registerCommand(vanPlugin, new MultiLoginCommand("multilogin", null, "login", "ml"));
        vanServer.getPluginManager().registerCommand(vanPlugin, new MultiLoginCommand("whitelist", null));
    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {
        Level vanLevel;
        if (level == LoggerLevel.ERROR) vanLevel = Level.SEVERE;
        else if (level == LoggerLevel.WARN) vanLevel = Level.WARNING;
        else if (level == LoggerLevel.INFO) vanLevel = Level.INFO;
        else if (level == LoggerLevel.DEBUG) return;
        else vanLevel = Level.INFO;
        vanPlugin.getLogger().log(vanLevel, message, throwable);
    }

    @Override
    public IServer getRunServer() {
        return server;
    }

    @Override
    public File getDataFolder() {
        return vanPlugin.getDataFolder();
    }

    @Override
    public String getPluginVersion() {
        return vanPlugin.getDescription().getVersion();
    }
}
