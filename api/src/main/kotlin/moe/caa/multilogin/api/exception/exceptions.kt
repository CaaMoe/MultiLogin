package moe.caa.multilogin.api.exception

sealed class APIException(message: String) : RuntimeException(message)

class ServiceDuplicateRegistrationException(
    message: String
) : APIException(message)

class ServiceIdDuplicateException(
    message: String
) : APIException(message)

class ServiceNotRegisteredException(
    message: String
) : APIException(message)


class LoginSourceRepeatSetException(
    message: String
) : APIException(message)

class LoginSourceInGameProfileMismatchException(
    message: String
) : APIException(message)

class UUIDDuplicationException(
    message: String
) : APIException(message)


class UsernameDuplicationException(
    message: String
) : APIException(message)

class InGameProfileNotFoundException(
    message: String
) : APIException(message)