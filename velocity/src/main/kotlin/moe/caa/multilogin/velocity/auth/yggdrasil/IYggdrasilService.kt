package moe.caa.multilogin.velocity.auth.yggdrasil

/**
 * 表示一个 Yggdrasil 类型的 AuthenticationService
 */
interface IYggdrasilService {
    suspend fun authenticate(username: String, serverId: String, playerIp: String): YggdrasilAuthenticationResult
}