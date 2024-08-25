plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "MultiLogin"

addProject("api")
addProject("velocity")

private fun addProject(name: String, path: String = name) {
    include(rootProject.name.lowercase() + "-" + name)
    project(":${rootProject.name.lowercase()}-$name").projectDir = file(path)
}
