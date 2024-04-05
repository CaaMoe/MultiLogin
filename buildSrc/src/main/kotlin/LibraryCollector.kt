package moe.caa.multilogin.gradle.librarycollector


//class LibraryCollector: Plugin<Project> {
//    override fun apply(target: Project) {
//        target.afterEvaluate {
//            val summaryCalculateConfiguration = target.configurations.register("summaryCalculate")
//            target.dependencies{
//                libraries.forEach {
//                    add(summaryCalculateConfiguration.name, mapOf(
//                        "group" to it.group,
//                        "name" to it.name,
//                        "version" to it.version,
//                        "transitive" to false
//                    ))
//                }
//            }
//
//            target.tasks.register("summaryCalculate"){
//                dependsOn(summaryCalculateConfiguration)
//                doLast {
//                    val digestedMap: MutableMap<String, String> = HashMap()
//                    configurations[summaryCalculateConfiguration.name].resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
//                        val dependency = artifact.moduleVersion.id
//                        val key = "${dependency.group}:${dependency.name}:${dependency.version}"
//                        digestedMap[key] = calculateDigest(artifact.file)
//                    }
//
//                    val file = target.layout.buildDirectory.file(".digested").get().asFile
//                    if (!file.parentFile.exists()) {
//                        file.parentFile.mkdirs()
//                    }
//                    file.writeText(digestedMap.entries.joinToString("\n") { "${it.key}=${it.value}" })
//                }
//            }
//        }
//    }
//
//    private fun calculateDigest(file: File): String {
//        val bytes = file.readBytes()
//        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
//        return digest.joinToString("") { "%02x".format(it) }
//    }
//}