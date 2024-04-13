package moe.caa.multilogin.gradle.librarycollector

fun annotations() = "org.jetbrains:annotations:${Versions.ANNOTATIONS}"
fun exposed(module: String) = "org.jetbrains.exposed:exposed-$module:${Versions.EXPOSED}"
fun cloud(module: String) = "org.incendo:cloud-$module:${Versions.CLOUD_COMMAND}"
fun serialization(module: String) =
    "org.jetbrains.kotlinx:kotlinx-serialization-$module:${Versions.KOTLINX_SERIALIZATION}"
fun spongeConfiguration(module: String) = "org.spongepowered:configurate-$module:${Versions.SPONGE_CONFIGURATION}"
fun adventure(module: String) = "net.kyori:adventure-$module:${Versions.ADVENTURE}"


fun velocity(module: String) = "com.velocitypowered:velocity-$module:${Versions.VELOCITY}"
fun netty(module: String) = "io.netty:netty-$module:${Versions.NETTY}"