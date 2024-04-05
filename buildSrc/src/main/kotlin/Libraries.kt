package moe.caa.multilogin.gradle.librarycollector

fun exposed(module: String) = "org.jetbrains.exposed:exposed-$module:${Versions.EXPOSED}"
fun cloud(module: String) = "org.incendo:cloud-$module:${Versions.CLOUD_COMMAND}"
fun serialization(module: String) = "org.jetbrains.kotlinx:kotlinx-serialization-$module:${Versions.KOTLINX_SERIALIZATION}"
