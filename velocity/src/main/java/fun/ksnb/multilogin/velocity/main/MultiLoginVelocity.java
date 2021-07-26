/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.main.MultiLoginVelocity
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.main;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import com.velocitypowered.proxy.VelocityServer;
import fun.ksnb.multilogin.velocity.listener.VelocityListener;
import moe.caa.multilogin.core.impl.AbstractScheduler;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.impl.ISender;
import moe.caa.multilogin.core.main.MultiCore;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Plugin(id = "multilogin", name = "MultiLogin", version = "@version@", authors = {"ksqeib", "CaaMoe"})
public class MultiLoginVelocity implements IPlugin {
    private static ProxyServer server;
    private static Logger logger;
    private final File dataDirectory;
    private static MultiLoginVelocity instance;
    private MultiCore core = new MultiCore(this);
    private VelocitySchedule velocitySchedule = new VelocitySchedule();

    @Override
    public void initCoreService() throws Throwable {

    }

    @Override
    public void initOtherService() {
//        基础监听
        server.getEventManager().register(this, new VelocityListener(core));
//        注册命令
        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("multilogin")
                .aliases("whitelist")
                .build();
        commandManager.register(meta, new MultiLoginCommand(core));
    }

    @Inject
    public MultiLoginVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory.toFile();
        instance = this;
    }

    public static MultiLoginVelocity getInstance() {
        return instance;
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        if (!core.init()) {
//            启动失败关闭
            core.disable();
        }
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
//        关闭事件
        core.disable();
    }

    public static void log(String log) {
        logger.info(log);
    }

    @Override
    public File getDataFolder() {
        return dataDirectory;
    }

    @Override
    public List<ISender> getOnlinePlayers() {
        return server.getAllPlayers().stream().map(VelocitySender::new).collect(Collectors.toList());
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getPluginVersion() {
        return server.getPluginManager().getPlugin("multilogin").get().getDescription().getVersion().get();
    }

    @Override
    public AbstractScheduler getSchedule() {
        return velocitySchedule;
    }

    @Override
    public boolean isOnlineMode() {
        return server.getConfiguration().isOnlineMode();
    }

    @Override
    public ISender getPlayer(UUID uuid) {
        return server.getPlayer(uuid).map(VelocitySender::new).orElse(null);
    }

    @Override
    public List<ISender> getPlayer(String name) {
        List<ISender> ret = new ArrayList<>();
        for (Player player : server.getAllPlayers()) {
            if (player.getUsername().equalsIgnoreCase(name)) ret.add(new VelocitySender(player));
        }
        return ret;
    }

    @Override
    public Gson getAuthGson() {
        return VelocityServer.GENERAL_GSON;
    }

    @Override
    public Type authResultType() {
        return GameProfile.class;
    }

    @Override
    public void shutdown() {
        server.shutdown();
    }

    @Override
    public MultiCore getMultiCore() {
        return core;
    }

    @Override
    public String getServerCoreName() {
        return server.getVersion().getName();
    }

    @Override
    public String getServerVersion() {
        return server.getVersion().getVersion();
    }
}
