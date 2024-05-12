package moe.caa.multilogin.core.configuration.service;

import lombok.Getter;
import moe.caa.multilogin.api.service.IService;
import moe.caa.multilogin.api.service.ServiceType;
import moe.caa.multilogin.core.configuration.ConfException;
import moe.caa.multilogin.core.configuration.SkinRestorerConfig;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.BiFunction;

@Getter
public abstract class BaseServiceConfig implements IService {
    private final int id;
    private final String name;
    private final InitUUID initUUID;
    private final String initNameFormat;
    private final boolean whitelist;
    private final SkinRestorerConfig skinRestorer;

    protected BaseServiceConfig(int id, String name, InitUUID initUUID, String initNameFormat,
                                boolean whitelist, SkinRestorerConfig skinRestorer) throws ConfException {
        this.id = id;
        this.name = name;
        this.initUUID = initUUID;
        this.initNameFormat = initNameFormat;
        this.whitelist = whitelist;
        this.skinRestorer = skinRestorer;

        checkValid();
    }

    protected void checkValid() throws ConfException {
        if (this.id > 127 || this.id < 0)
            throw new ConfException(String.format(
                    "Yggdrasil id %d is out of bounds, The value can only be between 0 and 127."
                    , this.id
            ));
    }

    public String generateName(String loginName){
        return initNameFormat.replace("{name}", loginName).replace(" ", "_");
    }

    @Override
    public int getServiceId() {
        return id;
    }

    @NotNull
    @Override
    public String getServiceName() {
        return name;
    }

    @NotNull
    public abstract ServiceType getServiceType();

    /**
     * 初始化的UUID生成器
     */
    public enum InitUUID {
        DEFAULT((u, n) -> u),
        OFFLINE((u, n) -> UUID.nameUUIDFromBytes(("OfflinePlayer:" + n).getBytes(StandardCharsets.UTF_8))),
        RANDOM((u, n) -> UUID.randomUUID());

        private final BiFunction<UUID, String, UUID> biFunction;

        InitUUID(BiFunction<UUID, String, UUID> biFunction) {
            this.biFunction = biFunction;
        }

        public UUID generateUUID(UUID onlineUUID, String currentUsername) {
            return biFunction.apply(onlineUUID, currentUsername);
        }
    }
}
