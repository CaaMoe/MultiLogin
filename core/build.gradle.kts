import moe.caa.multilogin.gradle.librarycollector.*

plugins {
    id("library-collector")
}

dependencies {
    databaseDrivers().forEach {
        summaryCalculate(it)
        implementation(it) // test
    }

    implementation(okhttp3())
    implementation(project(":api"))
    implementation(adventure("text-minimessage"))
    implementation(cloud("core"))
    implementation(spongeConfiguration("hocon"))
    implementation(spongeConfiguration("core"))
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

tasks.processResources {
    dependsOn(tasks["summaryCalculate"])

    from(layout.buildDirectory) {
        include(".digested")
    }
}