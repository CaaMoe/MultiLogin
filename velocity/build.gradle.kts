import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import moe.caa.multilogin.gradle.librarycollector.cloud
import moe.caa.multilogin.gradle.librarycollector.netty
import moe.caa.multilogin.gradle.librarycollector.velocity

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":loader"))
    implementation(cloud("velocity"))

    compileOnly(fileTree(File("libraries")))
    compileOnly(netty("all"))
    compileOnly(velocity("api"))
    annotationProcessor(velocity("api"))
}

tasks.shadowJar {
    archiveAppendix = "MultiLogin-Velocity"

    from (layout.buildDirectory){
        project(":core").tasks.shadowJar.get().archiveFile
    }
}

fun doRelocate(shadowJar: ShadowJar, pattern: String) {
    shadowJar.relocate(pattern, "moe.caa.multilogin.libraries.$pattern")
}
