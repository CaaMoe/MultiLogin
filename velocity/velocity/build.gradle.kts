import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import moe.caa.multilogin.gradle.librarycollector.velocity

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":multilogin-api"))
    implementation(project(":multilogin-loader"))

    compileOnly(velocity("api"))
    annotationProcessor(velocity("api"))
}

tasks.shadowJar {
    archiveAppendix = "MultiLogin-Velocity"

    copyShadowJar(this, project(":multilogin-core"))
    copyShadowJar(this, project(":multilogin-velocity-core"))
    copyShadowJar(this, project(":multilogin-loader"))
    copyShadowJar(this, project(":multilogin-api"))

    doLast {
        val jarDirectory = rootProject.file("jar")
        jarDirectory.mkdirs()

        archiveFile.get().asFile.copyTo(File(jarDirectory, archiveFile.get().asFile.name), overwrite = true)
    }
}

fun copyShadowJar(task: ShadowJar, project: Project) {
    task.dependsOn(project.tasks.shadowJar)
    val shadowJar = project.tasks.shadowJar.get()
    task.from(shadowJar.archiveFile) {
        include(shadowJar.archiveFileName.get())
    }
}