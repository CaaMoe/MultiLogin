package `fun`.iiii.multilogin.velocity.core.main

import `fun`.iiii.multilogin.velocity.bootstrap.MultiLoginVelocityBootstrap
import moe.caa.multilogin.loader.api.IPlatformCore

class MultiLoginVelocityCore(
    private val bootstrap: MultiLoginVelocityBootstrap
) : IPlatformCore<MultiLoginVelocityBootstrap> {
    override fun enable() {
        TODO("Not yet implemented")
    }

    override fun disable() {
        TODO("Not yet implemented")
    }


    override fun getBootstrap() = bootstrap
}