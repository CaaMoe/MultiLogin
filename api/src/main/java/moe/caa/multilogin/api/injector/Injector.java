package moe.caa.multilogin.api.injector;

import moe.caa.multilogin.api.main.MultiCoreAPI;

public interface Injector {
    void inject(MultiCoreAPI api) throws Throwable;
}
