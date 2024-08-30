package moe.caa.multilogin.velocity.auth.yggdrasil

data class LoginProfile(
    val username: String,
    val serverId: String,
    val playerIp: String
)
