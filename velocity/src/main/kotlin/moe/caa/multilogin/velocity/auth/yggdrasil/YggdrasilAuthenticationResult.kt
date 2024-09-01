package moe.caa.multilogin.velocity.auth.yggdrasil

import moe.caa.multilogin.api.profile.GameProfile
import moe.caa.multilogin.velocity.auth.validate.ValidateContext
import moe.caa.multilogin.velocity.config.service.yggdrasil.BaseYggdrasilService

/**
 * 表示 Yggdrasil 验证结果返回
 */
sealed interface YggdrasilAuthenticationResult {

    /**
     * Yggdrasil 验证成功返回
     */
    data class Success(
        val baseYggdrasilService: BaseYggdrasilService, val profile: GameProfile
    ) : YggdrasilAuthenticationResult {
        fun buildValidateContext() = ValidateContext(baseYggdrasilService, profile)
    }

    /**
     * 验证失败返回
     */
    data class Failure(val reason: Reason) : YggdrasilAuthenticationResult {

        // 从上往下, ordinal 越大 表示越严重
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

