package moe.caa.multilogin.api.internal.skinrestorer;

import moe.caa.multilogin.api.internal.auth.AuthResult;

public interface SkinRestorerAPI {

    /**
     * 进行皮肤修复
     */
    SkinRestorerResult doRestorer(AuthResult result);

}
