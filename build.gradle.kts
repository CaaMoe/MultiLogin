plugins {
    kotlin("jvm") version libs.versions.kotlinVer
    kotlin("plugin.serialization") version libs.versions.kotlinVer
    alias(libs.plugins.git)
}

allprojects {
    apply(plugin = "net.kyori.indra.git")

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


val outPutVer by extra(
    fun(): String {
        if (System.getProperty("build_type", "auto").equals("final", true)) {
            return version.toString()
        }
        val commitName = indraGit.commit()?.name()

        if (commitName != null) {
            return "Build_${commitName.substring(0, 6)}"
        }
        return "Build_unknown"
    }
)

