package moe.caa.multilogin.core.data;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public enum ConvUuid {

    DEFAULT,

    OFFLINE;

    public UUID getResultUuid(UUID onlineUuid, String name){
        if (this == DEFAULT) {
            return onlineUuid;
        }
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }
}