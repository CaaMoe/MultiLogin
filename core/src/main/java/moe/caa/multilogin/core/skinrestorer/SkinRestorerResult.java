package moe.caa.multilogin.core.skinrestorer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.auth.GameProfile;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class SkinRestorerResult {
    private final Reason reason;
    private final GameProfile response;
    private final Throwable throwable;

    public static SkinRestorerResult ofNoRestorer(){
        return new SkinRestorerResult(Reason.NO_RESTORER, null, null);
    }

    public static SkinRestorerResult ofSignatureValid(){
        return new SkinRestorerResult(Reason.SIGNATURE_VALID, null, null);
    }

    public static SkinRestorerResult ofRestorerSucceed(GameProfile profile){
        return new SkinRestorerResult(Reason.RESTORER_SUCCEED, profile, null);
    }

    public static SkinRestorerResult ofBadSkin(Throwable throwable){
        return new SkinRestorerResult(Reason.BAD_SKIN, null, throwable);
    }

    public static SkinRestorerResult ofRestorerFailed(Throwable throwable){
        return new SkinRestorerResult(Reason.RESTORER_FAILED, null, throwable);
    }

    public enum Reason {
        // 没有开启皮肤修复
        NO_RESTORER,

        // 皮肤签名有效，无需修复
        SIGNATURE_VALID,

        // 皮肤是烂的，比如高清皮或透明皮
        BAD_SKIN,

        // 皮肤修复成功
        RESTORER_SUCCEED,

        // 皮肤修复失败
        RESTORER_FAILED;
    }
}
