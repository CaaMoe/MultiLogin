import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

plugins {
    alias(libs.plugins.ideaext)
    alias(libs.plugins.eclipse)
}

tasks.register("transformBuildConstants"){
    doLast{
        val target = file("${layout.buildDirectory.get().asFile}/classes/kotlin/main/moe/caa/multilogin/api/build/BuildConstants.class")

        val reader = ClassReader(target.readBytes())
        val node = ClassNode()

        reader.accept(node, 0)

        for (field in node.fields){
            if(field.name.equals("VERSION")){
                field.value = "1.0"
            }
        }

        ClassWriter(0).apply {
            node.accept(this)
            target.writeBytes(this.toByteArray())
        }
    }
}

tasks.named("classes"){
    finalizedBy(tasks.named("transformBuildConstants"))
}

