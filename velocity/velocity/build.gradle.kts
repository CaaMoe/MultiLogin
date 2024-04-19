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

    dependsOn(project(":multilogin-core").tasks.shadowJar.get())
    dependsOn(project(":multilogin-velocity-core").tasks.shadowJar.get())

    from(project(":multilogin-core").tasks.shadowJar.get().archiveFile) {
        include(project(":multilogin-core").tasks.shadowJar.get().archiveFileName.get())
    }

    from(project(":multilogin-velocity-core").tasks.shadowJar.get().archiveFile) {
        include(project(":multilogin-velocity-core").tasks.shadowJar.get().archiveFileName.get())
    }

    dependencies {
        project(":multilogin-loader")
    }

    doLast {
        val jarDirectory = rootProject.file("jar")
        jarDirectory.mkdirs()

        archiveFile.get().asFile.copyTo(File(jarDirectory, archiveFile.get().asFile.name), overwrite = true)
    }
}