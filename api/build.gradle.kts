import moe.caa.multilogin.gradle.librarycollector.adventure
import moe.caa.multilogin.gradle.librarycollector.annotations
import moe.caa.multilogin.gradle.librarycollector.cloud


dependencies {
    compileOnly(cloud("core"))
    compileOnly(adventure("api"))
    compileOnly(annotations())

}