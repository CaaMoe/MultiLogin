package moe.caa.multilogin.velocity.manager

import moe.caa.multilogin.api.data.LoginSource
import moe.caa.multilogin.api.manager.LoginSourceDataManager
import moe.caa.multilogin.velocity.main.MultiLoginVelocity
import java.util.*

class VelocityLoginSourceDataManager(
    val plugin: MultiLoginVelocity
): LoginSourceDataManager {
    override fun findLoginSourceByInGameUUID(inGameUUID: UUID): LoginSource? {
        TODO("Not yet implemented")
    }
}