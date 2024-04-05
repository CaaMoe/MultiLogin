import net.kyori.blossom.TemplateSet


plugins {
    id("java")
    id("library-collector")

    alias(libs.plugins.blossom)
    alias(libs.plugins.git)
}

dependencies {
    compileOnly(project(":api"))

    // todo test
    implementation(project(":core"))
}

tasks.processResources {
    dependsOn("summaryCalculate")

    from(layout.buildDirectory) {
        include(".digested")
    }
}

sourceSets {
    main {
        blossom {
            javaSources { replace(this) }
            resources { replace(this) }
        }
    }
}

fun replace(it: TemplateSet) {
    val buildType = System.getProperty("build_type", "auto").lowercase()
    val version = if (buildType == "final") {
        project.version.toString()
    } else {
        "Build_" + (indraGit.commit()?.name ?: "unknown")
    }

    it.property("version", version)
    it.property("build_type", buildType)
    it.property("build_revision", indraGit.commit()?.name ?: "unknown")
    it.property("build_timestamp", System.currentTimeMillis().toString())
}