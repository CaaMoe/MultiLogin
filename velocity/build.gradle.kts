import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import moe.caa.multilogin.gradle.librarycollector.Versions
import moe.caa.multilogin.gradle.librarycollector.cloud

plugins {
    alias(libs.plugins.shadow)
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":core"))

    implementation(cloud("velocity"))

    compileOnly("com.velocitypowered:velocity-api:${Versions.VELOCITY_API}")
    annotationProcessor("com.velocitypowered:velocity-api:${Versions.VELOCITY_API}")
}

tasks.shadowJar {
    archiveBaseName = "MultiLogin-Velocity"
}


fun doRelocate(shadowJar: ShadowJar, pattern: String) {
    shadowJar.relocate(pattern, "moe.caa.multilogin.libraries.$pattern")
}
