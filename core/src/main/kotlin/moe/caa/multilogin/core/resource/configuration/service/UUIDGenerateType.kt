package moe.caa.multilogin.core.resource.configuration.service

import java.util.*

enum class UUIDGenerateType {
    ONLINE,
    OFFLINE,
    RANDOM;

    fun generateUUID(onlineUUID: UUID, currentUsername: String) = when (this) {
        ONLINE -> onlineUUID;
        OFFLINE -> UUID.nameUUIDFromBytes("OfflinePlayer:$currentUsername".toByteArray(Charsets.UTF_8));
        RANDOM -> UUID.randomUUID()
    }
}