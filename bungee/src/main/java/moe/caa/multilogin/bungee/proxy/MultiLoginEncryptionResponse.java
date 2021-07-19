package moe.caa.multilogin.bungee.proxy;

import com.google.common.base.Preconditions;
import moe.caa.multilogin.bungee.auth.BungeeAuthTask;
import moe.caa.multilogin.bungee.main.MultiLoginBungee;
import moe.caa.multilogin.core.language.LanguageKeys;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
根据EncryptionResponse编写的认证拦截器
 */
public class MultiLoginEncryptionResponse extends EncryptionResponse {
    public static Class<?> INITIAL_HANDLER_CLASS_STATE_CLASS;
    public static MethodHandle THIS_STATE;
    public static MethodHandle REQUEST;
    public static MethodHandle CHANNEL_WRAPPER;
    public static InitialHandler initialHandler;
    public static SecretKey sharedKey;
    public static EncryptionRequest request;

    public static void init() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Class<InitialHandler> INITIAL_HANDLER_CLASS = InitialHandler.class;
        INITIAL_HANDLER_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
        THIS_STATE = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, INITIAL_HANDLER_CLASS_STATE_CLASS, false));
        REQUEST = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, EncryptionRequest.class, false));
        CHANNEL_WRAPPER = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(INITIAL_HANDLER_CLASS, ChannelWrapper.class, false));
    }


    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
//        处理
        if (!(handler instanceof InitialHandler)) {
            handler.handle(this);
            return;
        }
        initialHandler = (InitialHandler) handler;
        try {
            request = (EncryptionRequest) REQUEST.invoke(handler);
            addEncrypt();
//            交给登入任务处理
            BungeeCord.getInstance().getScheduler().runAsync(MultiLoginBungee.plugin, new BungeeAuthTask(initialHandler, getUsername(), getServerId(), getIp()));
        } catch (Throwable e) {
            e.printStackTrace();
            initialHandler.disconnect(new TextComponent(LanguageKeys.VERIFICATION_NO_ADAPTER.getMessage()));
            MultiLogger.log(LoggerLevel.ERROR, e);
            MultiLogger.log(LoggerLevel.ERROR, LanguageKeys.ERROR_AUTH.getMessage());
        }

    }

    //    添加正版加密
    private void addEncrypt() throws Throwable {
        ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.invoke(initialHandler);
        Preconditions.checkState(THIS_STATE.invoke(initialHandler) == ReflectUtil.getEnumIns((Class<? extends Enum<?>>) INITIAL_HANDLER_CLASS_STATE_CLASS, "ENCRYPT"), "Not expecting ENCRYPT");
        sharedKey = EncryptionUtil.getSecret(this, request);
        if (sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length != 16) {
            ch.close();
            return;
        }
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
    }

    //    解密服务器ID
    public String getServerId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return decode(request.getServerId().getBytes("ISO_8859_1"));
    }

    //    获取玩家名
    public String getUsername() {
        return initialHandler.getName();
    }

    //    获取IP
    public String getIp() {
        if (BungeeCord.getInstance().config.isPreventProxyConnections() && initialHandler.getSocketAddress() instanceof InetSocketAddress) {
            return initialHandler.getAddress().getAddress().getHostAddress();
        }
        return null;
    }

    //    解密数据
    private String decode(byte[] message) throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][]{message, sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()}) {
            sha.update(bit);
        }
        return (new BigInteger(sha.digest())).toString(16);
    }
}
