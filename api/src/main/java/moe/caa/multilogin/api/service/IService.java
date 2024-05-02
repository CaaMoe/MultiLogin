package moe.caa.multilogin.api.service;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 表示一个 MultiLogin 的身份验证服务
 */
@ApiStatus.NonExtendable
public interface IService {

    /**
     * 返回验证服务类型
     * @return 验证服务类型
     */
    @NotNull ServiceType getServiceType();

    /**
     * 返回验证服务名字
     * @return 返回验证服务名字
     */
    @NotNull String getServiceName();

    /**
     * 返回验证服务ID
     * @return 验证服务ID
     */
    int getServiceId();
}
