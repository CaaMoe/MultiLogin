rootProject.name = "MultiLogin"

proj("loader")
proj("api")
proj("core")


proj("velocity", "velocity/velocity")
proj("velocity-core", "velocity/velocity-core")


fun proj(name: String, filePath: String = name){
    include("multilogin-$name")
    project(":multilogin-$name").projectDir = file(filePath)
}