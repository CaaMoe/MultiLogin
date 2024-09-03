package moe.caa.multilogin.velocity.config.service.yggdrasil

import java.io.File

class OfflineYggdrasilService (
    resourceFile: File,
    baseServiceSetting: BaseServiceSetting,
    yggdrasilServiceSetting: YggdrasilServiceSetting
): BaseYggdrasilService(
    resourceFile, baseServiceSetting,
    yggdrasilServiceSetting, CustomYggdrasilServiceSetting(
        HasJoinedRequestMode.GET, OFFLINE_YGGDRASIL_HAS_JOINED_URL_BYTES,
        linkedMapOf(
            "username" to "{username}",
            "serverId" to "{serverId}",
            "ip" to "{playerIp}",
        )
    )
) {
    companion object {
        // https://sessionserver.mojang.com/session/minecraft/hasJoined
        private val OFFLINE_YGGDRASIL_HAS_JOINED_URL_BYTES: ByteArray = byteArrayOf(
            104,
            116,
            116,
            112,
            115,
            58,
            47,
            47,
            115,
            101,
            115,
            115,
            105,
            111,
            110,
            115,
            101,
            114,
            118,
            101,
            114,
            46,
            109,
            111,
            106,
            97,
            110,
            103,
            46,
            99,
            111,
            109,
            47,
            115,
            101,
            115,
            115,
            105,
            111,
            110,
            47,
            109,
            105,
            110,
            101,
            99,
            114,
            97,
            102,
            116,
            47,
            104,
            97,
            115,
            74,
            111,
            105,
            110,
            101,
            100
        )
    }
}

class BlessingSkinYggdrasilService (
    resourceFile: File,
    baseServiceSetting: BaseServiceSetting,
    yggdrasilServiceSetting: YggdrasilServiceSetting,
    blessingSkinYggdrasilServiceSetting: BlessingSkinYggdrasilServiceSetting
): BaseYggdrasilService(
    resourceFile, baseServiceSetting,
    yggdrasilServiceSetting, CustomYggdrasilServiceSetting(
        HasJoinedRequestMode.GET,
        blessingSkinYggdrasilServiceSetting.yggdrasilApiRoot.trim('/').toByteArray()
            .plus(SECTION_SESSION_SERVER_HAS_JOINED_PATH_BYTES),
        linkedMapOf(
            "username" to "{username}",
            "serverId" to "{serverId}",
            "ip" to "{playerIp}",
        )
    )
) {
    companion object {
        // /sessionserver/session/minecraft/hasJoined
        private val SECTION_SESSION_SERVER_HAS_JOINED_PATH_BYTES: ByteArray = byteArrayOf(
            47,
            115,
            101,
            115,
            115,
            105,
            111,
            110,
            115,
            101,
            114,
            118,
            101,
            114,
            47,
            115,
            101,
            115,
            115,
            105,
            111,
            110,
            47,
            109,
            105,
            110,
            101,
            99,
            114,
            97,
            102,
            116,
            47,
            104,
            97,
            115,
            74,
            111,
            105,
            110,
            101,
            100
        )
    }
}

data class BlessingSkinYggdrasilServiceSetting(
    val yggdrasilApiRoot: String
)