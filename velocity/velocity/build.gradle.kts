import moe.caa.multilogin.gradle.librarycollector.velocity

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":loader"))

    compileOnly(velocity("api"))
    annotationProcessor(velocity("api"))
}

tasks.shadowJar {
    dependsOn(project(":core").tasks.shadowJar.get())
    dependsOn(project(":velocity-core").tasks.shadowJar.get())

    archiveAppendix = "MultiLogin-Velocity"

    from(project(":core").tasks.shadowJar.get().archiveFile) {
        include(project(":core").tasks.shadowJar.get().archiveFileName.get())
    }

    from(project(":velocity-core").tasks.shadowJar.get().archiveFile) {
        include(project(":velocity-core").tasks.shadowJar.get().archiveFileName.get())
    }
}