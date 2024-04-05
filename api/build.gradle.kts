import moe.caa.multilogin.gradle.librarycollector.Versions
import moe.caa.multilogin.gradle.librarycollector.cloud


dependencies {
    implementation ("org.jetbrains:annotations:${Versions.ANNOTATIONS}")
    implementation(cloud("core"))

    compileOnly ("net.kyori:adventure-api:${Versions.ADVENTURE_API}")
}