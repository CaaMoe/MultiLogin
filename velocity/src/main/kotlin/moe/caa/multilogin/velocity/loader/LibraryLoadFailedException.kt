package moe.caa.multilogin.velocity.loader

import io.ktor.utils.io.errors.*

class LibraryLoadFailedException(msg: String, throwable: Throwable? = null): IOException(msg, throwable)