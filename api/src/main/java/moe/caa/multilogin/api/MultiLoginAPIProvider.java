package moe.caa.multilogin.api;

import lombok.Getter;

public class MultiLoginAPIProvider {
    @Getter
    private static MultiLoginAPI api;

    public synchronized static void setApi(MultiLoginAPI api) {
        if (MultiLoginAPIProvider.api != null) throw new UnsupportedOperationException("duplicate api.");
        MultiLoginAPIProvider.api = api;
    }
}
