package moe.caa.multilogin.api;

import moe.caa.multilogin.api.data.IProfileData;
import moe.caa.multilogin.api.data.IUserData;
import moe.caa.multilogin.api.player.MultiLoginPlayerData;
import moe.caa.multilogin.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * MultiLogin API
 *
 * @since 0.8.0
 */
@ApiStatus.NonExtendable
public interface MultiLoginAPI {

    /**
     * 返回所有已注册的 Service
     * @return 所有已注册的 Service
     */
    @NotNull
    List<IService> getServices();

    /**
     * 获取在线玩家的游戏数据
     * @param profileUuid 需要获取的玩家的游戏内 UUID
     * @return 玩家的游戏数据
     */
    @Nullable
    MultiLoginPlayerData getPlayerData(UUID profileUuid);

    @Nullable
    IService getService(int serviceId);

    // 擦车白名单相关
    boolean addCacheWhitelist(@NotNull String loginUsername);
    boolean addCacheWhitelist(@NotNull String loginUsername, @NotNull IService service);
    boolean hasCacheWhitelist(@NotNull String loginUsername);
    boolean hasCacheWhitelist(@NotNull String loginUsername, @NotNull IService service);
    boolean removeCacheWhitelist(@NotNull String loginUsername);
    boolean removeCacheWhitelist(@NotNull String loginUsername, @NotNull IService service);

    // 玩家数据相关(仅查询)
    @NotNull List<IUserData> findAllUserData();
    @NotNull List<IUserData> findAllUserData(@NotNull IService service);
    @Nullable IUserData findUserData(int userId);
    @Nullable IUserData findUserData(@NotNull UUID loginUUID, @NotNull IService service);

    // 档案数据相关(仅查询)
    @NotNull List<IProfileData> findAllProfileData();
    @Nullable IProfileData findProfileData(int profileId);
    @Nullable IProfileData findProfileData(@NotNull UUID profileUuid);
    @Nullable IProfileData findProfileData(@NotNull String profileUsername);

    // 混合查询相关(仅查询)
    @Nullable IUserData findWhoInitializedIt(@NotNull IProfileData profileData);
    @Nullable List<IUserData> findLinker(@NotNull IProfileData profileData);

    // 档案操作相关
    @NotNull IProfileData createProfile(@NotNull UUID profileUUID, @NotNull String profileName);
    @NotNull IProfileData renameProfile(@NotNull IProfileData handle, @NotNull String newProfileName);

    // 玩家数据操作相关
    @NotNull IUserData setLinkToProfile(@NotNull IUserData userData, @NotNull IProfileData profileData);
    @NotNull IUserData setLinkToInitialProfile(@NotNull IUserData userData);
    @NotNull IUserData setWhitelist(@NotNull IUserData handle, boolean whitelist);
}
