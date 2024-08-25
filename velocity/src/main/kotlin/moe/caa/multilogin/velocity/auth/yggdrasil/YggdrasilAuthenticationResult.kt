package moe.caa.multilogin.velocity.auth.yggdrasil

import moe.caa.multilogin.api.profile.GameProfile

/**
 * 表示 Yggdrasil 验证结果返回
 */
sealed interface YggdrasilAuthenticationResult {

    /**
     * Yggdrasil 验证成功返回
     */
    class Success(val profile: GameProfile): YggdrasilAuthenticationResult

    /**
     * 验证失败返回
     */
    class Failure(val reason: Reason): YggdrasilAuthenticationResult{

        enum class Reason {
            // 没有找到任何有效的 Yggdrasil Service
            NO_YGGDRASIL_SERVICES,
            // 无效的会话
            INVALID_SESSION,
            // 服务器无法连接
            SERVER_BREAK_DOWN,
        }
    }
}

