package moe.caa.multilogin.api.internal.auth;

import org.jetbrains.annotations.ApiStatus;

/**
 * 验证 API
 */
@ApiStatus.Internal
public interface AuthAPI {

    /**
     * 进行验证
     *
     * @param username 用户名
     * @param serverId 服务器ID
     * @param ip       用户IP
     * @return 验证结果
     */
    AuthResult auth(String username, String serverId, String ip);
}
