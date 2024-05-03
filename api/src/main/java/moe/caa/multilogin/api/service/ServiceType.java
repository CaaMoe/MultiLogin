package moe.caa.multilogin.api.service;

public enum ServiceType {
    OFFICIAL,
    BLESSING_SKIN,
    CUSTOM_YGGDRASIL,
    FLOODGATE;

    public boolean allowedDuplicate(){
        return switch (this){
            case OFFICIAL, FLOODGATE -> false;
            default -> true;
        };
    }
}
