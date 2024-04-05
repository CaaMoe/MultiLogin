package moe.caa.multilogin.core.resource.configuration

import java.io.IOException

class ReadConfigurationException : IOException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
    constructor() : super()
}
