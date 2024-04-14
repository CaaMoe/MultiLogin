package moe.caa.multilogin.api.auth

data class LoginProfile(
    val username: String,
    val serverId: String,
    val playerIp: String
)
