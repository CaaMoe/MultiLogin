import moe.caa.multilogin.gradle.librarycollector.Versions
import moe.caa.multilogin.gradle.librarycollector.cloud
import moe.caa.multilogin.gradle.librarycollector.exposed
import moe.caa.multilogin.gradle.librarycollector.serialization

dependencies {
    compileOnly("net.kyori:adventure-api:${Versions.ADVENTURE_API}")

    implementation(project(":api"))
    implementation(cloud("core"))

    implementation("mysql:mysql-connector-java:${Versions.MYSQL_CONNECTOR}")


    implementation("org.spongepowered:configurate-hocon:${Versions.SPONGE_CONFIGURATION}")
    implementation("org.spongepowered:configurate-core:${Versions.SPONGE_CONFIGURATION}")

    implementation(serialization("json"))
    implementation(exposed("core"))
    implementation(exposed("crypt"))
    implementation(exposed("dao"))
    implementation(exposed("jdbc"))
    implementation(exposed("jodatime"))
    implementation(exposed("java-time"))
    implementation(exposed("kotlin-datetime"))
    implementation(exposed("json"))
    implementation(exposed("money"))
    implementation(exposed("spring-boot-starter"))
}