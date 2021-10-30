package moe.caa.multilogin.fabric.auth;

import com.mojang.authlib.GameProfile;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class MultiLoginGameProfile extends GameProfile {

    @Getter
    @Setter
    private String disconnectMessage;

    public MultiLoginGameProfile(UUID id, String name) {
        super(id, name);
    }
}
