package moe.caa.multilogin.core.auth

data class LoginProfile(
    val username: String,
    val serverId: String,
    val playerIp: String
)
