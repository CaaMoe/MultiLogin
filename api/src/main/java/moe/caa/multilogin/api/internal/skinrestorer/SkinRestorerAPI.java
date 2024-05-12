package moe.caa.multilogin.api.internal.skinrestorer;

import moe.caa.multilogin.api.internal.auth.AuthResult;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface SkinRestorerAPI {

    /**
     * 进行皮肤修复
     */
    SkinRestorerResult doRestorer(AuthResult result);

}
