plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("library-collector") {
            id = "library-collector"
            implementationClass = "moe.caa.multilogin.gradle.librarycollector.LibraryCollector"
        }
    }
}