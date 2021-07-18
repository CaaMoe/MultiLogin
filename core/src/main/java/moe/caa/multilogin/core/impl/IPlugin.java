package moe.caa.multilogin.core.impl;

import com.google.gson.Gson;
import moe.caa.multilogin.core.auth.Verifier;
import moe.caa.multilogin.core.data.User;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 代表一个插件对象
 */
public interface IPlugin {

    /**
     * 获得插件数据文件夹
     *
     * @return 插件数据文件夹
     */
    File getDataFolder();

    /**
     * 获得插件Jar包文件流
     *
     * @param path Jar包文件路径
     * @return 对应的文件流
     */
    InputStream getJarResource(String path);

    /**
     * 获得当前所有在线的玩家列表
     *
     * @return 在线玩家列表
     */
    List<ISender> getOnlinePlayers();

    /**
     * 获得插件的日志记录器
     *
     * @return 日志记录器
     */
    Logger getLogger();

    /**
     * 获得插件版本号
     *
     * @return 插件版本号
     */
    String getPluginVersion();

    /**
     * 获得线程调度器对象
     *
     * @return 调度器对象
     */
    ISchedule getSchedule();

    /**
     * 是否开启在线验证
     *
     * @return 在线验证模式
     */
    boolean isOnlineMode();

    /**
     * 获得在线玩家
     *
     * @param uuid uuid
     * @return 玩家
     */
    ISender getPlayer(UUID uuid);

    /**
     * 获得在线玩家
     */
    List<ISender> getPlayer(String name);

    /**
     * 获得原版验证时的 GSON 对象
     */
    Gson getAuthGson();

    Type authResultType();

    void initCoreService() throws Throwable;

    void initOtherService();

    void shutdown();

    default boolean onAsyncLoginSuccess(UUID uuid, String name) {
        return Verifier.CACHE_LOGIN.remove(uuid, name);
    }

    default void onLeave() {
        getSchedule().runTask(() -> Verifier.CACHE_USER.removeIf(user -> getPlayer(user.redirectUuid) == null));
    }

    default void onJoin(ISender player) {
        Verifier.CACHE_USER.removeIf(user -> getPlayer(user.redirectUuid) == null);
        if (player.isOp() || player.hasPermission("")) {
            // TODO: 2021/7/18 发送更新信息
        }
    }

    default User getCacheUserData(UUID redirectUuid) {
        for (User user : Verifier.CACHE_USER) {
            if (user.redirectUuid.equals(redirectUuid)) {
                return user;
            }
        }
        return null;
    }
}
