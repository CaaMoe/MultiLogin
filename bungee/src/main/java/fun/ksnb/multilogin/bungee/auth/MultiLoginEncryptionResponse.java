package fun.ksnb.multilogin.bungee.auth;

import com.google.common.base.Preconditions;
import fun.ksnb.multilogin.bungee.impl.BungeeUserLogin;
import fun.ksnb.multilogin.bungee.main.MultiLoginBungeePluginBootstrap;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;
import moe.caa.multilogin.core.util.FormatContent;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MultiLoginEncryptionResponse extends EncryptionResponse {
    private static Class<?> INITIAL_HANDLER_CLASS_STATE_CLASS;
    private static MethodHandle THIS_STATE;
    private static MethodHandle REQUEST;
    private static MethodHandle CHANNEL_WRAPPER;
    private SecretKey sharedKey;
    private EncryptionRequest request;

    public static void init() throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Class<InitialHandler> INITIAL_HANDLER_CLASS = InitialHandler.class;
        INITIAL_HANDLER_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        THIS_STATE = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.getField(INITIAL_HANDLER_CLASS, INITIAL_HANDLER_CLASS_STATE_CLASS), true));
        REQUEST = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.getField(INITIAL_HANDLER_CLASS, EncryptionRequest.class), true));
        CHANNEL_WRAPPER = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.getField(INITIAL_HANDLER_CLASS, ChannelWrapper.class), true));
    }


    @Override
    public void handle(AbstractPacketHandler handler) throws Exception {
        if (!(handler instanceof InitialHandler)) {
            handler.handle(this);
            return;
        }

        try {
            addEncrypt((InitialHandler) handler);
        } catch (Throwable throwable) {
            ((InitialHandler) handler).disconnect(MultiLoginBungeePluginBootstrap.getInstance().getCore().getLanguageHandler().getMessage("auth_error", FormatContent.empty()));
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception while processing encryption.", throwable);
            MultiLogger.getLogger().log(LoggerLevel.ERROR, "handler: " + handler);
        }

        MultiLoginBungeePluginBootstrap.getInstance().getRunServer().getScheduler().runTaskAsync(() -> {
            try {
                MultiLoginBungeePluginBootstrap.getInstance().getCore().getAuthCore().doAuth(new BungeeUserLogin(getUsername((InitialHandler) handler), getServerId(), getIp((InitialHandler) handler), (InitialHandler) handler));
            } catch (Exception e) {
                ((InitialHandler) handler).disconnect(MultiLoginBungeePluginBootstrap.getInstance().getCore().getLanguageHandler().getMessage("auth_error", FormatContent.empty()));
                MultiLogger.getLogger().log(LoggerLevel.ERROR, "An exception occurred while processing login data.", e);
                MultiLogger.getLogger().log(LoggerLevel.ERROR, "handler: " + handler);
            }

        });
    }

    private void addEncrypt(InitialHandler handler) throws Throwable {
        Preconditions.checkState(
                THIS_STATE.invoke(handler) == ReflectUtil.getEnumIns((Class<? extends Enum<?>>) INITIAL_HANDLER_CLASS_STATE_CLASS
                        , "ENCRYPT"), "Not expecting ENCRYPT");

        request = (EncryptionRequest) REQUEST.invoke(handler);
        sharedKey = EncryptionUtil.getSecret(this, request);

        ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.invoke(handler);

        SecretKey sharedKey = EncryptionUtil.getSecret(this, this.request);
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
    }

    public String getServerId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[][] sharedSecret = new byte[][]{this.request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};
        for (byte[] bit : sharedSecret) {
            sha.update(bit);
        }
        return URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
    }

    public String getUsername(InitialHandler handler) throws UnsupportedEncodingException {
        return handler.getName();
    }

    public String getIp(InitialHandler handler) {
        if (BungeeCord.getInstance().config.isPreventProxyConnections() && handler.getSocketAddress() instanceof InetSocketAddress) {
            return handler.getAddress().getAddress().getHostAddress();
        }
        return null;
    }
}
