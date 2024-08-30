import org.apache.tools.ant.filters.ReplaceTokens

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

    implementation(libs.cloudcore)
    implementation(libs.cloudveloity)
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
}