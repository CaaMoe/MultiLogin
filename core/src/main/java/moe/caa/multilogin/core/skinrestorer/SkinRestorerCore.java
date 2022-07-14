package moe.caa.multilogin.core.skinrestorer;

import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.auth.Property;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class SkinRestorerCore {
    private final YggdrasilServiceConfig yggdrasilServiceConfig;
    private final OkHttpClient okHttpClient;
    private final GameProfile profile;

    public SkinRestorerCore(YggdrasilServiceConfig yggdrasilServiceConfig, GameProfile profile) {
        this.yggdrasilServiceConfig = yggdrasilServiceConfig;
        this.profile = profile.clone();
        this.okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new RetryInterceptor(yggdrasilServiceConfig.getSkinRestorer().getRetry(),
                        yggdrasilServiceConfig.getSkinRestorer().getRetryDelay()))
                .addInterceptor(new LoggingInterceptor())
                .writeTimeout(Duration.ofMillis(yggdrasilServiceConfig.getSkinRestorer().getTimeout()))
                .readTimeout(Duration.ofMillis(yggdrasilServiceConfig.getSkinRestorer().getTimeout()))
                .connectTimeout(Duration.ofMillis(yggdrasilServiceConfig.getSkinRestorer().getTimeout()))
                .proxy(yggdrasilServiceConfig.getSkinRestorer().getProxy().getProxy())
                .proxyAuthenticator(yggdrasilServiceConfig.getSkinRestorer().getProxy().getProxyAuthenticator())
                .build();
    }

    public SkinRestorerResult doRestorer(){
        Map<String, Property> propertyMap = profile.getPropertyMap();
        if(propertyMap == null){
            return SkinRestorerResult.ofNoSkin();
        }
        return SkinRestorerResult.ofNoRestorer();
    }

    private byte[] requireValidSkin(String skinUrl, boolean slim) throws IOException {
        Request request = new Request.Builder()
                .get()
                .url(skinUrl)
                .build();
        // 下载皮肤原件
        byte[] bytes = Objects.requireNonNull(okHttpClient.newCall(request).execute().body()).bytes();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)){
            BufferedImage image = ImageIO.read(bais);

            boolean x64 = false; // 1.8 新版皮肤
            if (image.getWidth() != 64) {
                throw new SkinRestorerException("Skin width is not 64.");
            }
            if(!(image.getHeight() == 32 || image.getHeight() == 64)){
                throw new SkinRestorerException("Skin height is not 64 or 32.");
            }
            x64 = image.getHeight() == 64;
            // TODO: 2022/7/13 皮肤半透明判断

            return bytes;
        }
    }
}
