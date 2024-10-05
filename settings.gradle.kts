rootProject.name = "MultiLogin"

include("multilogin-api")
project(":multilogin-api").projectDir = file("api")

include("multilogin-velocity")
project(":multilogin-velocity").projectDir = file("velocity")

include("multilogin-velocity-core")
project(":multilogin-velocity-core").projectDir = file("velocity/core")
