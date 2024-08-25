//import org.objectweb.asm.ClassReader
//import org.objectweb.asm.ClassWriter
//import org.objectweb.asm.tree.ClassNode
//
//
//tasks.register("transformBuildConstants") {
//    doLast {
//        val target =
//            file("${layout.buildDirectory.get().asFile}/classes/kotlin/main/moe/caa/multilogin/api/build/BuildConstants.class")
//        val reader = ClassReader(target.readBytes())
//        val node = ClassNode()
//
//        reader.accept(node, 0)
//
//        val replaceMap =  mapOf(
//            "VERSION" to "1.0"
//        )
//
//        for (field in node.fields) {
//            replaceMap[field.name]?.apply {
//                field.value = this
//            }
//        }
//
//        ClassWriter(0).apply {
//            node.accept(this)
//            target.writeBytes(this.toByteArray())
//        }
//    }
//}
//
//tasks.named("classes") {
//    finalizedBy(tasks.named("transformBuildConstants"))
//}
//
