package moe.caa.multilogin.gradle.librarycollector

fun annotations() = "org.jetbrains:annotations:${Versions.ANNOTATIONS}"
fun exposed(module: String) = "org.jetbrains.exposed:exposed-$module:${Versions.EXPOSED}"
fun cloud(module: String) = "org.incendo:cloud-$module:${Versions.CLOUD_COMMAND}"
fun serialization(module: String) =
    "org.jetbrains.kotlinx:kotlinx-serialization-$module:${Versions.KOTLINX_SERIALIZATION}"
fun spongeConfiguration(module: String) = "org.spongepowered:configurate-$module:${Versions.SPONGE_CONFIGURATION}"
fun adventure(module: String) = "net.kyori:adventure-$module:${Versions.ADVENTURE}"
fun okhttp3() = "com.squareup.okhttp3:okhttp:${Versions.OKHTTP3}"


fun velocity(module: String) = "com.velocitypowered:velocity-$module:${Versions.VELOCITY}"
fun netty(module: String) = "io.netty:netty-$module:${Versions.NETTY}"

fun databaseDrivers() = listOf(
    "com.impossibl.pgjdbc-ng:pgjdbc-ng:0.8.9",
    "org.postgresql:postgresql:42.7.3",

    "mysql:mysql-connector-java:8.0.33",
    "org.mariadb.jdbc:mariadb-java-client:3.3.3",
    "org.xerial:sqlite-jdbc:3.45.2.0",
    "com.h2database:h2:2.2.224",
    "com.microsoft.sqlserver:mssql-jdbc:12.6.1.jre11",
)