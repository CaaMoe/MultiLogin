package moe.caa.multilogin.api;

import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public class MultiLoginAPIProvider {
    @Getter
    private static MultiLoginAPI api;

    @ApiStatus.Internal
    public synchronized static void setApi(MultiLoginAPI api) {
        if (MultiLoginAPIProvider.api != null) throw new UnsupportedOperationException("duplicate api.");
        MultiLoginAPIProvider.api = api;
    }
}
