rootProject.name = "MultiLogin"

include("loader")
include("api")
include("core")


include("velocity")
include("velocity-core")

project(":velocity").projectDir = file("velocity/velocity")
project(":velocity-core").projectDir = file("velocity/velocity-core")

project(":api").apply {
    name = "multilogin-api"
}