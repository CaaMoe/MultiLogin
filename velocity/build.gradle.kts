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
    archiveFileName.set("MultiLogin-Velocity-${getOutputVersion()}.jar")

    doLast {
        project.rootProject.file("output").mkdirs()
        run {
            File(project.rootProject.file("output"), archiveFileName.get()).outputStream().use { output ->
                archiveFile.get().asFile.inputStream().use { input -> input.transferTo(output) }
            }
        }
    }
}

fun getOutputVersion(): String {
    if (System.getProperty("build_type", "auto").equals("final", true)) {
        return version.toString()
    }
    val commitName = indraGit.commit()?.name()

    if (commitName != null) {
        return "Build_${commitName.substring(0, 6)}"
    }
    return "Build_unknown"
}


fun doRelocate(shadowJar: ShadowJar, pattern: String) {
    shadowJar.relocate(pattern, "moe.caa.multilogin.libraries.$pattern")
}
