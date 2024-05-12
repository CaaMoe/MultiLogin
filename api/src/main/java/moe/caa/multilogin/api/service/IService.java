package moe.caa.multilogin.api.service;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 表示一个验证服务器
 */
@ApiStatus.NonExtendable
public interface IService {

    /**
     * 返回这个验证服务ID
     * @return 这个验证服务ID
     */
    int getServiceId();

    /**
     * 返回验证服务名字
     * @return 验证服务名字
     */
    @NotNull String getServiceName();

    /**
     * 返回验证服务类型
     * @return 验证服务类型
     */
    @NotNull ServiceType getServiceType();
}
