package moe.caa.multilogin.api.plugin;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface ExtendedService {
    void enable();

    void disable();
}
