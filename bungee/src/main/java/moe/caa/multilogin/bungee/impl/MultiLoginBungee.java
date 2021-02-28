package moe.caa.multilogin.bungee.impl;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TObjectIntMap;
import moe.caa.multilogin.bungee.Metrics;
import moe.caa.multilogin.bungee.listener.BungeeListener;
import moe.caa.multilogin.bungee.proxy.MultiLoginEncryptionResponse;
import moe.caa.multilogin.bungee.task.AuthTask;
import moe.caa.multilogin.core.IConfiguration;
import moe.caa.multilogin.core.IPlugin;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 蹦极端的插件主类
 */
public class MultiLoginBungee extends Plugin implements IPlugin {
    public static File configFile;
    public static MultiLoginBungee INSTANCE;
    private final ScheduledExecutorService TIMER = Executors.newScheduledThreadPool(10);
    private BungeeConfiguration configuration;

    /**
     * 修改服务
     */
    private void initCoreService() throws Exception {
        MultiLoginEncryptionResponse.init();
        AuthTask.init();

        Class<MultiLoginEncryptionResponse> packetClass = MultiLoginEncryptionResponse.class;
        int packetID = 0x01;

        Class<?> protocol_directionDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol_protocolDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");

        Field field_protocols = ReflectUtil.getField(protocol_directionDataClass, "protocols");
        Field field_TO_SERVER = ReflectUtil.getField(Protocol.class, "TO_SERVER");
        Field field_packetMap = ReflectUtil.getField(protocol_protocolDataClass, "packetMap");
        Field field_packetConstructors = ReflectUtil.getField(protocol_protocolDataClass, "packetConstructors");
        Object to_server = field_TO_SERVER.get(Protocol.LOGIN);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) field_protocols.get(to_server);
        for (int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            if (protocol >= 47) {
                Object data = protocols.get(protocol);
                TObjectIntMap<Class<? extends DefinedPacket>> packetMap = (TObjectIntMap) field_packetMap.get(data);
                packetMap.remove(EncryptionResponse.class);
                packetMap.put(packetClass, packetID);
                //2021/2/28 Fixed Supplier unsupported problem
                Object[] constructors = (Object[]) field_packetConstructors.get(data);
                if (constructors instanceof Supplier[]) {
                    Supplier<? extends DefinedPacket>[] suppliers = (Supplier<? extends DefinedPacket>[]) constructors;
                    suppliers[packetID] = MultiLoginEncryptionResponse::new;
                } else if (constructors instanceof Constructor[]) {
                    constructors[packetID] = MultiLoginEncryptionResponse.class.getDeclaredConstructor();
                } else {
                    throw new UnsupportedOperationException(String.format("不兼容的BungeeCord : %s %s", getProxy().getName(), getProxy().getVersion()));
                }
            }
        }
    }

    @Override
    public void onEnable() {
        MultiLoginBungee.INSTANCE = this;
        configFile = new File(this.getPluginDataFolder(), "config.yml");

        try {
            initCoreService();
        } catch (Throwable e) {
            e.printStackTrace();
            getLogger().severe("初始化修改失败，插件可能不兼容您的服务端！");
            BungeeCord.getInstance().stop();
            return;
        }

        BungeeCord.getInstance().getPluginManager().registerListener(this, new BungeeListener());

        BungeeCord.getInstance().getPluginManager().registerCommand(this, new Command("whitelist") {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                MultiCore.submitCommand("whitelist", new BungeeSender(commandSender), strings);
            }
        });
        BungeeCord.getInstance().getPluginManager().registerCommand(this, new Command("multilogin") {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                MultiCore.submitCommand("multilogin", new BungeeSender(commandSender), strings);
            }
        });

        if (!MultiCore.initService(this)) {
            BungeeCord.getInstance().stop();
            return;
        }

        new Metrics(this, 9888);
        getLogger().info("插件已加载");
    }

    @Override
    public void onDisable() {
        try {
            TIMER.shutdown();
        } catch (Exception ignored) {
        }
        MultiCore.disable();
        BungeeCord.getInstance().stop();
    }

    @Override
    public File getPluginDataFolder() {
        return getDataFolder();
    }

    @Override
    public IConfiguration getPluginConfig() {
        return configuration;
    }

    @Override
    public void savePluginDefaultConfig() {
        if (configFile.exists()) return;
        try (InputStream input = getPluginResource("config.yml"); FileOutputStream fOut = new FileOutputStream(configFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                fOut.write(buf, 0, len);
            }
        } catch (Exception e) {
            getPluginLogger().log(Level.SEVERE, "无法保存文件 " + configFile.getName());
        }
    }

    @Override
    public void reloadPluginConfig() {
        try {
            configuration = new BungeeConfiguration(ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml")));
        } catch (Exception ignore) {
            getPluginLogger().log(Level.SEVERE, "无法读取文件 " + configFile.getName());
        }
    }

    @Override
    public IConfiguration yamlLoadConfiguration(InputStreamReader reader) throws IOException {
        return new BungeeConfiguration(ConfigurationProvider.getProvider(YamlConfiguration.class).load(reader));
    }

    @Override
    public InputStream getPluginResource(String path) {
        return getResourceAsStream(path);
    }

    @Override
    public void kickPlayer(UUID uuid, String msg) {
        ProxiedPlayer player = BungeeCord.getInstance().getPlayer(uuid);
        if (player != null) {
            player.disconnect(new TextComponent(msg));
        }
    }

    @Override
    public Logger getPluginLogger() {
        return getLogger();
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void runTaskAsyncLater(Runnable run, long delay) {
        TIMER.schedule(run, delay * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTaskAsyncTimer(Runnable run, long delay, long per) {
        TIMER.scheduleAtFixedRate(run, delay * 50, per * 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void runTask(Runnable run, long delay) {
        BungeeCord.getInstance().getScheduler().schedule(this, run, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public Map<UUID, String> getOnlineList() {
        Map<UUID, String> ret = new HashMap<>();
        for (ProxiedPlayer player : BungeeCord.getInstance().getPlayers()) {
            ret.put(player.getUniqueId(), player.getName());
        }
        return ret;
    }
}
