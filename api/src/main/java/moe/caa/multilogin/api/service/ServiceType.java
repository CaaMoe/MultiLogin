package moe.caa.multilogin.api.service;

import org.jetbrains.annotations.ApiStatus;

/**
 * 验证服务类型
 */
@ApiStatus.NonExtendable
public enum ServiceType {

    /**
     * 官方 Yggdrasil Java 版账号验证服务（Yggdrasil 实现）。
     */
    OFFICIAL(true),

    /**
     * Blessing Skin 的伪正版验证服务（Yggdrasil 实现）。
     */
    BLESSING_SKIN(true),

    /**
     * 自定义 Yggdrasil 伪正版验证服务（Yggdrasil 实现）。
     */
    CUSTOM_YGGDRASIL(true),

    /**
     * Geyser 的 Floodgate （Xbox账号）验证服务。
     */
    FLOODGATE(false);

    private final boolean yggdrasilService;

    ServiceType(boolean yggdrasilService) {
        this.yggdrasilService = yggdrasilService;
    }

    public boolean isYggdrasilService() {
        return yggdrasilService;
    }
}
