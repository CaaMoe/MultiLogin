plugins {
    kotlin("jvm") version libs.versions.kotlinVer
    kotlin("plugin.serialization") version libs.versions.kotlinVer
}

allprojects {
    group = "moe.caa"
    version = "0.8.0"

    repositories {
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    java.sourceCompatibility = JavaVersion.VERSION_17
    java.targetCompatibility = JavaVersion.VERSION_17
}