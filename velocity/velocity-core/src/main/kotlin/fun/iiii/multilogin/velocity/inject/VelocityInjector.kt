package `fun`.iiii.multilogin.velocity.inject

import `fun`.iiii.multilogin.velocity.inject.netty.MultiLoginChannelInitializer
import `fun`.iiii.multilogin.velocity.main.MultiLoginVelocity

class VelocityInjector(val plugin: MultiLoginVelocity) {
    fun inject() {
        MultiLoginChannelInitializer.init(plugin)
    }
}