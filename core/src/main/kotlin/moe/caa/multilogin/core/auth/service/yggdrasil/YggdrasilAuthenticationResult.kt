package moe.caa.multilogin.core.auth.service.yggdrasil

import moe.caa.multilogin.core.resource.configuration.service.BaseService

class YggdrasilAuthenticationResult(
    val reason: Reason,
    val service: BaseService?,
    val gameProfile: Any?
) {
    companion object {
        fun ofNoYggdrasilService() = YggdrasilAuthenticationResult(Reason.NO_YGGDRASIL_SERVICE, null, null)
    }

    enum class Reason() {
        // 通过
        ALLOWED,

        // 服务器通讯失败
        SERVER_BREAKDOWN,

        // 无效的会话
        INVALID_SESSION,
        NO_YGGDRASIL_SERVICE
    }
}