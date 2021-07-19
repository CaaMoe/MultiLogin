/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.main.MultiLoginBungee
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.main;

import com.google.gson.Gson;
import gnu.trove.map.TIntObjectMap;
import moe.caa.multilogin.bungee.auth.BungeeAuthTask;
import moe.caa.multilogin.bungee.listener.BungeeListener;
import moe.caa.multilogin.bungee.proxy.MultiLoginEncryptionResponse;
import moe.caa.multilogin.core.command.CommandHandler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.impl.Scheduler;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MultiLoginBungee extends Plugin implements IPlugin {
    public static BungeeSchedule schedule;
    public static MultiLoginBungee plugin;
    public CommandHandler commandHandler;

    @Override
    public void initCoreService() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
//        bungeecord核心初始化
        MultiLoginEncryptionResponse.init();
        BungeeAuthTask.init();

        Class<?> protocol_directionDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$DirectionData");
        Class<?> protocol_protocolDataClass = Class.forName("net.md_5.bungee.protocol.Protocol$ProtocolData");

        Field field_protocols = ReflectUtil.getField(protocol_directionDataClass, "protocols", true);
        Field field_TO_SERVER = ReflectUtil.getField(Protocol.class, "TO_SERVER", true);
        Field field_packetConstructors = ReflectUtil.getField(protocol_protocolDataClass, "packetConstructors", true);
        Object to_server = field_TO_SERVER.get(Protocol.LOGIN);
        TIntObjectMap<?> protocols = (TIntObjectMap<?>) field_protocols.get(to_server);
        for (int protocol : ProtocolConstants.SUPPORTED_VERSION_IDS) {
            if (protocol >= 47) {
                Object data = protocols.get(protocol);
                //2021/2/28 Fixed Supplier unsupported problem
                Object[] constructors = (Object[]) field_packetConstructors.get(data);
                if (constructors instanceof Supplier[]) {
                    Supplier<? extends DefinedPacket>[] suppliers = (Supplier<? extends DefinedPacket>[]) constructors;
                    suppliers[0x01] = MultiLoginEncryptionResponse::new;
                } else if (constructors instanceof Constructor[]) {
                    constructors[0x01] = MultiLoginEncryptionResponse.class.getDeclaredConstructor();
                } else {
                    throw new UnsupportedOperationException(LanguageKeys.ERROR_REDIRECT_MODIFY.getMessage());
                }
            }
        }
    }

    @Override
    public void initOtherService() {
//        注册其他服务需要的监听
        getProxy().getPluginManager().registerListener(this, new BungeeListener());

        BungeeCord.getInstance().getPluginManager().registerCommand(this, new MultiLoginCommand("whitelist"));
        BungeeCord.getInstance().getPluginManager().registerCommand(this, new MultiLoginCommand("multilogin"));
    }

    @Override
    public void onEnable() {
        plugin = this;
        schedule = new BungeeSchedule(this);
        if (!MultiCore.init(this)) {
//            启动失败关闭
            onDisable();
        }
    }

    @Override
    public void onDisable() {
        MultiCore.disable();
    }

    @Override
    public InputStream getJarResource(String path) {
        return getResourceAsStream(path);
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return getProxy().getPlayers().stream().map(BungeeSender::new).collect(Collectors.toList());
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public Scheduler getSchedule() {
        return schedule;
    }

    @Override
    public boolean isOnlineMode() {
        return getProxy().getConfig().isOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        return new BungeeSender(getProxy().getPlayer(uuid));
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (ProxiedPlayer player : getProxy().getPlayers()) {
            if (player.getName().equalsIgnoreCase(name)) ret.add(new BungeeSender(player));
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return BungeeCord.getInstance().gson;
    }

    @Override
    public Type authResultType() {
        return LoginResult.class;
    }

    @Override
    public void shutdown() {
        BungeeCord.getInstance().stop();
    }
}
