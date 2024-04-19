package `fun`.iiii.multilogin.velocity.inject

import `fun`.iiii.multilogin.velocity.inject.netty.MultiLoginChannelInitializer
import `fun`.iiii.multilogin.velocity.main.MultiLoginVelocityCore

class VelocityInjector(val plugin: MultiLoginVelocityCore) {
    fun inject() {
        MultiLoginChannelInitializer.init(plugin)
    }
}