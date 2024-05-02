package moe.caa.multilogin.api;

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


}
