package moe.caa.multilogin.core.resource.configuration.service

enum class ServiceType {
    OFFICIAL,
    BLESSING_SKIN,
    CUSTOM_YGGDRASIL,
    FLOODGATE;

    fun allowedDuplicate(): Boolean {
        return when (this) {
            BLESSING_SKIN, CUSTOM_YGGDRASIL -> true
            OFFICIAL, FLOODGATE -> false
        }
    }
}