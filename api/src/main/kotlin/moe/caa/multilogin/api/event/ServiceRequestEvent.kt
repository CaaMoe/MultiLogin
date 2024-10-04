package moe.caa.multilogin.api.event

import moe.caa.multilogin.api.service.BaseService

/**
 * 当一个普通客户端开始登录时触发这个事件(LoginHello包接收时), (不包括基岩玩家)
 */
class ServiceRequestEvent {

    /**
     * 指定需要验证它的 Service 是谁.
     *
     * 如果此值为空, 则将轮询 YggdrasilServices
     */
    var assignService: BaseService? = null



    // 登录数据
    data class LoginData(
        // todo netty 的 channel 什么的

        // 客户端链接到服务器所使用的 address
        val clientLinkAddress: String,
        // 客户端链接到服务器所使用的 端口
        val clientLinkPort: Int,
    )
}