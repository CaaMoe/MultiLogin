package moe.caa.multilogin.bungee.proxy;

import com.google.common.base.Preconditions;
import moe.caa.multilogin.bungee.impl.MultiLoginBungee;
import moe.caa.multilogin.bungee.task.AuthTask;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
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
import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;

public class MultiLoginEncryptionResponse extends EncryptionResponse {
    private static Class<?> INITIAL_HANDLE_CLASS_STATE_CLASS;
    private static MethodHandle THIS_STATE;
    private static MethodHandle REQUEST;
    private static MethodHandle CHANNEL_WRAPPER;

    public static void init() throws ClassNotFoundException, IllegalAccessException {
        Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
        INITIAL_HANDLE_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
        THIS_STATE = ReflectUtil.getFieldUnReflectGetter(INITIAL_HANDLE_CLASS, INITIAL_HANDLE_CLASS_STATE_CLASS);
        REQUEST = ReflectUtil.getFieldUnReflectGetter(INITIAL_HANDLE_CLASS, EncryptionRequest.class);
        CHANNEL_WRAPPER = ReflectUtil.getFieldUnReflectGetter(INITIAL_HANDLE_CLASS, ChannelWrapper.class);
    }

    public void handle(AbstractPacketHandler handler) throws Exception {
        if (!(handler instanceof InitialHandler)) {
            handler.handle(this);
            return;
        }
        InitialHandler initialHandler = (InitialHandler) handler;
        try {
            EncryptionRequest request = (EncryptionRequest) REQUEST.invoke(handler);
            ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.invoke(handler);
            Preconditions.checkState(THIS_STATE.invoke(handler) == ReflectUtil.getEnumIns(INITIAL_HANDLE_CLASS_STATE_CLASS, "ENCRYPT"), "Not expecting ENCRYPT");
            SecretKey sharedKey = EncryptionUtil.getSecret(this, request);
            if (sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length != 16) {
                ch.close();
                return;
            }
            BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
            ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
            BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
            ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
            String encName = URLEncoder.encode(initialHandler.getName(), "UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[][] var7 = new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};

            for (byte[] bit : var7) {
                sha.update(bit);
            }
            String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
            String preventProxy = BungeeCord.getInstance().config.isPreventProxyConnections() && initialHandler.getSocketAddress() instanceof InetSocketAddress ? "&ip=" + URLEncoder.encode(initialHandler.getAddress().getAddress().getHostAddress(), "UTF-8") : "";
            String arg = String.format("hasJoined?username=%s&serverId=%s%s", encName, encodedHash, preventProxy);

            BungeeCord.getInstance().getScheduler().runAsync(MultiLoginBungee.INSTANCE, new AuthTask(initialHandler, arg));
        } catch (Throwable e) {
            e.printStackTrace();
            initialHandler.disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
            MultiCore.getPlugin().getPluginLogger().severe("处理用户数据时出现异常");
        }
    }
}
