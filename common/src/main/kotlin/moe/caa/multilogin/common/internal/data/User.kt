package moe.caa.multilogin.common.internal.data

import java.util.*

@JvmRecord
data class User(
    @JvmField val userID: Int,
    @JvmField val loginMethod: String,
    @JvmField val userUUID: UUID,
    @JvmField val username: String
) {
    val displayName: String
        get() = "$username(id: $userID, login method: $loginMethod)"
}
