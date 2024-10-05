dependencies {
    compileOnly(
        fileTree(
            mapOf(
                "dir" to "${rootProject.projectDir.absolutePath}/velocity/libraries",
                "include" to listOf("*.jar")
            )
        )
    )

    "com.velocitypowered:velocity-api:3.3.0-SNAPSHOT".apply {
        annotationProcessor(this)
        compileOnly(this)
    }

    compileOnly(project(":multilogin-api"))
    compileOnly(project(":multilogin-velocity"))
    compileOnly(libs.exposedcore)
    compileOnly(libs.exposeddao)
    compileOnly(libs.exposedjdbc)
    compileOnly(libs.hikaricp)
}

tasks.shadowJar {
    archiveFileName.set("MultiLogin-Velocity-Core")
}

artifacts {
    archives(tasks.shadowJar)
}