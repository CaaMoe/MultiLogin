package moe.caa.multilogin.core.auth.service.yggdrasil

data class LoginProfile(
    val username: String,
    val serverId: String,
    val playerIp: String
)
