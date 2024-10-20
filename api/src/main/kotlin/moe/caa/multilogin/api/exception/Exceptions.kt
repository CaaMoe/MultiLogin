package moe.caa.multilogin.api.exception

open class APIException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ProfileConflictException(message: String, cause: Throwable? = null) : APIException(message, cause)

class ServiceDuplicateRegistrationException(message: String, cause: Throwable? = null) : APIException(message, cause)

class ServiceIdDuplicateException(message: String, cause: Throwable? = null) : APIException(message, cause)

class ServiceNotRegisteredException(message: String, cause: Throwable? = null) : APIException(message, cause)

class LoginSourceRepeatSetException(message: String, cause: Throwable? = null) : APIException(message, cause)

class LoginSourceInGameProfileMismatchException(message: String, cause: Throwable? = null) : APIException(message, cause)

class InGameProfileNotFoundException(message: String, cause: Throwable? = null) : APIException(message, cause)