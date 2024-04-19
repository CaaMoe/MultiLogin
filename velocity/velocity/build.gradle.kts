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
    dependsOn(project(":multilogin-core").tasks.shadowJar.get())
    dependsOn(project(":multilogin-velocity-core").tasks.shadowJar.get())

    archiveAppendix = "MultiLogin-Velocity"

    from(project(":multilogin-core").tasks.shadowJar.get().archiveFile) {
        include(project(":multilogin-core").tasks.shadowJar.get().archiveFileName.get())
    }

    from(project(":multilogin-velocity-core").tasks.shadowJar.get().archiveFile) {
        include(project(":multilogin-velocity-core").tasks.shadowJar.get().archiveFileName.get())
    }
}