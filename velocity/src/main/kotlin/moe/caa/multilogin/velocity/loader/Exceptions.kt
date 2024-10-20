package moe.caa.multilogin.velocity.loader

sealed class LibraryLoadException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class UnknownGroupLibraryException(message: String, cause: Throwable? = null) : LibraryLoadException(message, cause)

class LibraryCalculationFailedException(message: String, cause: Throwable? = null) : LibraryLoadException(message, cause)

class LibraryDownloadFailedException(message: String, cause: Throwable? = null) : LibraryLoadException(message, cause)