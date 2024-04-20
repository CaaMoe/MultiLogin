package moe.caa.multilogin.loader.api;

public interface IPlatformCore<BOOTSTRAP extends IBootstrap> extends ExtendedService {
    BOOTSTRAP getBootstrap();
}
