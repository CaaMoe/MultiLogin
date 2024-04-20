package `fun`.iiii.multilogin.velocity.core.inject

import `fun`.iiii.multilogin.velocity.core.inject.netty.MultiLoginChannelInitializer
import `fun`.iiii.multilogin.velocity.core.main.MultiLoginVelocityCore

class VelocityInjector(val plugin: MultiLoginVelocityCore) {
    fun inject() {
        MultiLoginChannelInitializer.init(plugin)
    }
}