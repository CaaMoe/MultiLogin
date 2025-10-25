package moe.caa.multilogin.common.internal.data

import java.util.*

@JvmRecord
data class Profile(
    @JvmField val profileID: Int,
    @JvmField val ownerUserID: Int,
    @JvmField val profileSlot: Int,
    @JvmField val profileUUID: UUID,
    @JvmField val profileName: String
) {
    val displayName: String
        get() = "$profileName(owner: $ownerUserID, slot: $profileSlot, uuid: $profileUUID)"
}
