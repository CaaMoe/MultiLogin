import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import moe.caa.multilogin.gradle.librarycollector.Versions
import moe.caa.multilogin.gradle.librarycollector.cloud

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(fileTree(File("libraries")))

    implementation(project(":api"))
    implementation(project(":core"))

    implementation(cloud("velocity"))

    implementation(kotlin("reflect"))

    implementation("io.netty:netty-all:${Versions.NETTY}")

    compileOnly("com.velocitypowered:velocity-api:${Versions.VELOCITY_API}")
    annotationProcessor("com.velocitypowered:velocity-api:${Versions.VELOCITY_API}")
}

tasks.shadowJar {
    archiveAppendix = "MultiLogin-Velocity"
}

fun doRelocate(shadowJar: ShadowJar, pattern: String) {
    shadowJar.relocate(pattern, "moe.caa.multilogin.libraries.$pattern")
}
