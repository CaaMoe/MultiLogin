package moe.caa.multilogin.api.data;

import moe.caa.multilogin.api.service.IService;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 表示一个用户数据
 */
@ApiStatus.NonExtendable
public interface IUserData {

    /**
     * 表示用户在服务器上的唯一ID(不区分验证服务器)
     *
     * @return 用户在服务器上的唯一ID(不区分验证服务器)
     */
    int getUserId();

    /**
     * 返回用户所使用的验证服务实例
     *
     * @return 用户所使用的验证服务实例
     */
    @Nullable
    IService getService();


    UUID getLoginUUID();
    String getLoginUsername();
    boolean hasWhitelist();
    IProfileData getInitialProfileData();
    IProfileData getLinkToProfileData();
}
