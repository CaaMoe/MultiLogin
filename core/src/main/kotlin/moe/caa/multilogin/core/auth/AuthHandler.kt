package moe.caa.multilogin.core.auth

import moe.caa.multilogin.core.auth.service.yggdrasil.YggdrasilAuthenticator
import moe.caa.multilogin.core.main.MultiCore

class AuthHandler(
    val multiCore: MultiCore
) {
    val yggdrasilAuthenticator = YggdrasilAuthenticator(this)


}