package moe.caa.multilogin.api.internal.skinrestorer;

import moe.caa.multilogin.api.profile.GameProfile;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerResult {

    Reason getReason();

    GameProfile getResponse();

    Throwable getThrowable();

    enum Reason {
        // 档案里面没有皮肤
        NO_SKIN,

        // 没有开启皮肤修复
        NO_RESTORER,

        // 使用缓存的皮肤修复数据
        USE_CACHE,

        // 皮肤签名有效，无需修复
        SIGNATURE_VALID,

        // 皮肤是烂的，比如高清皮或透明皮
        BAD_SKIN,

        // 皮肤修复成功
        RESTORER_SUCCEED,

        // 非阻塞式修复
        RESTORER_ASYNC,

        // 皮肤修复失败
        RESTORER_FAILED;
    }
}
