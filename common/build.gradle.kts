plugins {
    id("org.jetbrains.kotlin.jvm") version libs.versions.kotlin
}

dependencies {
    compileOnly(libs.slf4j.api)
    compileOnly(libs.adventure.api)
    compileOnly(libs.adventure.text.minimessage)
    compileOnly(libs.brigadier)


    compileOnly(libs.kotlin.stdlib)
    compileOnly(libs.kotlin.reflect)
    compileOnly(libs.hikaricp)
    compileOnly(libs.exposed.core)
    compileOnly(libs.exposed.jdbc)
    compileOnly(libs.exposed.java.time)
    compileOnly(libs.exposed.migration.core)
    compileOnly(libs.exposed.migration.jdbc)
    compileOnly(libs.configurate.core)
    compileOnly(libs.configurate.hocon)
    compileOnly(libs.typesafe)
}

tasks.jar { enabled = true }
tasks.shadowJar { enabled = false }