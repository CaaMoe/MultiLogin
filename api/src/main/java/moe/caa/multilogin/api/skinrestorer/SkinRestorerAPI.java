package moe.caa.multilogin.api.skinrestorer;

import moe.caa.multilogin.api.auth.AuthResult;

public interface SkinRestorerAPI {

    /**
     * 进行皮肤修复
     */
    SkinRestorerResult doRestorer(AuthResult result);

}
