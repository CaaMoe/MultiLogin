import groovy.json.JsonOutput

plugins {
    id("library-collector")
}

dependencies {
    compileOnly(project(":multilogin-api"))
}

val buildData: Map<String, String> by rootProject.extra

tasks.processResources {
    dependsOn("summaryCalculate")

    from(layout.buildDirectory) {
        include(".digested")
    }

    layout.buildDirectory.file("builddata").get().asFile.apply {
        parentFile?.mkdirs()
    }.writeText(
        JsonOutput.toJson(buildData)
    )
    from(layout.buildDirectory) {
        include("builddata")
    }
}