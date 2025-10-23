import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml

plugins {
    id("io.papermc.paperweight.userdev") version libs.versions.paperweight.userdev
    id("xyz.jpenilla.run-paper") version libs.versions.run.paper
    id("xyz.jpenilla.resource-factory-paper-convention") version libs.versions.resource.factory.paper.convention
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

tasks.runServer {
    minecraftVersion("1.21.8")
}

dependencies {
    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")

    implementation(project(":common"))

    compileOnlyAndExtra(libs.kotlin.stdlib)
    compileOnlyAndExtra(libs.kotlin.reflect)
    compileOnlyAndExtra(libs.kotlinx.datetime)
    compileOnlyAndExtra(libs.exposed.core)
    compileOnlyAndExtra(libs.exposed.jdbc)
    compileOnlyAndExtra(libs.exposed.java.time)
    compileOnlyAndExtra(libs.exposed.kotlin.time)
    compileOnlyAndExtra(libs.exposed.migration.core)
    compileOnlyAndExtra(libs.exposed.migration.jdbc)
    compileOnlyAndExtra(libs.hikaricp)
    compileOnlyAndExtra(libs.configurate.core)
    compileOnlyAndExtra(libs.configurate.hocon)
    compileOnlyAndExtra(libs.typesafe)
}

fun DependencyHandler.compileOnlyAndExtra(dependencyNotation: Any) {
    add("compileOnly", dependencyNotation)
    add("extra", dependencyNotation)
}

paperPluginYaml {
    name = project.rootProject.name
    main = "moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain"
    loader = "moe.caa.multilogin.paper.internal.bootstrap.MultiLoginPaperLoader"
    apiVersion = "1.21"
    version = project.version.toString()
    contributors = project.rootDir.resolve("config").resolve("contributors.txt").readLines().filter {
        it.isNotEmpty()
    }
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
}

