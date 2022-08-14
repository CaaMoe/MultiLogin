package moe.caa.multilogin.core.skinrestorer;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.logger.LoggerProvider;

/**
 * 皮肤修复结果
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class SkinRestorerResult {
    private final Reason reason;
    private final GameProfile response;
    private final Throwable throwable;

    public static SkinRestorerResult ofNoSkin() {
        return new SkinRestorerResult(Reason.NO_SKIN, null, null);
    }

    public static SkinRestorerResult ofNoRestorer() {
        return new SkinRestorerResult(Reason.NO_RESTORER, null, null);
    }

    public static SkinRestorerResult ofSignatureValid() {
        return new SkinRestorerResult(Reason.SIGNATURE_VALID, null, null);
    }

    public static SkinRestorerResult ofRestorerAsync() {
        return new SkinRestorerResult(Reason.RESTORER_ASYNC, null, null);
    }

    public static SkinRestorerResult ofUseCache(GameProfile profile) {
        return new SkinRestorerResult(Reason.USE_CACHE, profile, null);
    }

    public static SkinRestorerResult ofRestorerSucceed(GameProfile profile) {
        return new SkinRestorerResult(Reason.RESTORER_SUCCEED, profile, null);
    }

    public static SkinRestorerResult ofBadSkin(Throwable throwable) {
        return new SkinRestorerResult(Reason.BAD_SKIN, null, throwable);
    }

    public static SkinRestorerResult ofRestorerFailed(Throwable throwable) {
        return new SkinRestorerResult(Reason.RESTORER_FAILED, null, throwable);
    }

    public static void handleSkinRestoreResult(Throwable throwable) {
        LoggerProvider.getLogger().error("An exception occurred while processing the skin repair.", throwable);
    }

    public static void handleSkinRestoreResult(SkinRestorerResult result) {
        if (result.getThrowable() != null) {
            handleSkinRestoreResult(result.getThrowable());
        }
    }

    public enum Reason {
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
