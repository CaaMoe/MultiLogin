package moe.caa.multilogin.core.skinrestorer;

import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.core.configuration.yggdrasil.YggdrasilServiceConfig;
import moe.caa.multilogin.core.ohc.LoggingInterceptor;
import moe.caa.multilogin.core.ohc.RetryInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Objects;

public class SkinRestorerCore {
    private final YggdrasilServiceConfig yggdrasilServiceConfig;
    private final OkHttpClient okHttpClient;

    public SkinRestorerCore(YggdrasilServiceConfig yggdrasilServiceConfig, GameProfile profile) {
        this.yggdrasilServiceConfig = yggdrasilServiceConfig;
        okHttpClient = new OkHttpClient.Builder()
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
        return SkinRestorerResult.ofNoRestorer();
    }

    public byte[] requireValidSkin(String skinUrl) throws IOException {
        Request request = new Request.Builder()
                .get()
                .url(skinUrl)
                .build();
        // 下载皮肤原件
        InputStream inputStream = Objects.requireNonNull(okHttpClient.newCall(request).execute().body()).byteStream();
        BufferedImage image = ImageIO.read(inputStream);

        boolean x64 = false; // 1.8 新版皮肤
        if (image.getWidth() != 64) {
            throw new SkinRestorerException("Skin width is not 64.");
        }
        if(!(image.getHeight() == 32 || image.getHeight() == 64)){
            throw new SkinRestorerException("Skin height is not 64 or 32.");
        }
        x64 = image.getHeight() == 64;
        // TODO: 2022/7/13 皮肤半透明判断

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            ImageIO.write(image, "PNG", baos);
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new SkinRestorerException("Failed to save skin image.", e);
        }
    }
}
