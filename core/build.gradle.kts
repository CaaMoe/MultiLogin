import moe.caa.multilogin.gradle.librarycollector.*


dependencies {
    compileOnly(project(":multilogin-api"))

    compileOnly(okhttp3())
    compileOnly(adventure("text-minimessage"))
    compileOnly(cloud("core"))
    compileOnly(spongeConfiguration("hocon"))
    compileOnly(spongeConfiguration("core"))
    compileOnly(serialization("json"))
    compileOnly(exposed("core"))
    compileOnly(exposed("crypt"))
    compileOnly(exposed("dao"))
    compileOnly(exposed("jdbc"))
    compileOnly(exposed("jodatime"))
    compileOnly(exposed("java-time"))
    compileOnly(exposed("kotlin-datetime"))
    compileOnly(exposed("json"))
    compileOnly(exposed("money"))
    compileOnly(exposed("spring-boot-starter"))
}

tasks.shadowJar {
    archiveFileName = "MultiLogin-Core"
}