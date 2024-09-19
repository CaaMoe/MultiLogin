import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.internal.impldep.org.bouncycastle.util.encoders.UTF8
import org.jetbrains.kotlin.daemon.md5Digest

val digestConfiguration = configurations.register("shouldDigest")
fun DependencyHandler.shouldDigest(dependencyNotation: Any) = add("shouldDigest", dependencyNotation)

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    implementation("io.ktor:ktor-client-cio-jvm:2.3.12")
    // velocity core
    compileOnly(fileTree(mapOf("dir" to "libraries", "include" to listOf("*.jar"))))
    compileOnly(libs.velocityapi)
    annotationProcessor(libs.velocityapi)


    implementation(project(":multilogin-api"))
    implementation(libs.kotlincoroutinescore)
    implementation(libs.kotlinxserializationjson)

    implementation(libs.exposedcore)
    implementation(libs.exposeddao)
    implementation(libs.exposedjdbc)
    implementation(libs.exposedjavatime)
    implementation(libs.exposedkotlindatetime)
    implementation(libs.hikaricp)
    implementation(libs.mysql)

    implementation(libs.ktorclientcore)
    implementation(libs.ktorclientlogging)
    implementation(libs.ktorclientcio)


    rootProject.file(".group_libraries").readLines(Charsets.UTF_8)
        .filter { !it.trim().startsWith("#") }
        .filter { it.trim().isNotEmpty() }
        .forEach { shouldDigest(it) }
}

val buildData: Map<String, String> by rootProject.extra
tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    filesMatching("velocity-plugin.json") {
        filter(
            ReplaceTokens::class, mapOf(
                "tokens" to buildData,
                "beginToken" to "@",
                "endToken" to "@"
            )
        )
    }

    layout.buildDirectory.file(".group_libraries_digested").get().asFile.apply {
        parentFile?.mkdirs()

        this.writeText(HashMap<String, String>().apply {
            digestConfiguration.get().resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                val id = artifact.moduleVersion.id
                val key = "${id.group}:${id.name}:${id.version}"
                this[key] = artifact.file.md5Digest().joinToString("") { "%02x".format(it) }
            }
        }.entries.joinToString("\n") { "${it.key}=${it.value}" }, Charsets.UTF_8)
    }

    from(rootProject.file(".group_libraries")) {
        include(".group_libraries")
    }
    from(layout.buildDirectory) {
        include(".group_libraries_digested")
    }
}