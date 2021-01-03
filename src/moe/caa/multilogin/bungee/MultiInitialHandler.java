package moe.caa.multilogin.bungee;

import com.google.common.base.Preconditions;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;

public class MultiInitialHandler extends InitialHandler{
    private static final String AUTH_ARG = "hasJoined?username=%s&serverId=%s%s";
    private final Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
    private final Class INITIAL_HANDLE_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
    private final Field THIS_STATE = RefUtil.getField(INITIAL_HANDLE_CLASS, INITIAL_HANDLE_CLASS_STATE_CLASS);
    private final Field REQUEST = RefUtil.getField(INITIAL_HANDLE_CLASS, EncryptionRequest.class);
    private final Field CHANNEL_WRAPPER = RefUtil.getField(INITIAL_HANDLE_CLASS, ChannelWrapper.class);
    private final Field LOGIN_REQUEST = RefUtil.getField(INITIAL_HANDLE_CLASS, LoginRequest.class);
    private final Field NAME = RefUtil.getField(INITIAL_HANDLE_CLASS, "name");
    private final Field UNIQUE_ID = RefUtil.getField(INITIAL_HANDLE_CLASS, "uniqueId");
    private final BungeeCord BUNGEE;
    private final ListenerInfo listener;


    private final Method FINISH = RefUtil.getMethod(INITIAL_HANDLE_CLASS, "finish");

    private final InitialHandler vanHandle;

    public MultiInitialHandler(BungeeCord bungee, ListenerInfo listener, InitialHandler vanHandle) throws ClassNotFoundException, NoSuchFieldException {
        super(bungee, listener);
        this.vanHandle = vanHandle;
        this.BUNGEE = bungee;
        this.listener = listener;
    }


    @Override
    public void handle(EncryptionResponse encryptResponse) throws Exception {
        EncryptionRequest request = (EncryptionRequest) REQUEST.get(vanHandle);
        ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.get(vanHandle);
        Preconditions.checkState(THIS_STATE.get(vanHandle) == RefUtil.getEnumIns(INITIAL_HANDLE_CLASS_STATE_CLASS, "ENCRYPT"), "Not expecting ENCRYPT");
        SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);
        if (sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length != 16) {
            ch.close();
        } else {
            BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
            ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
            BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
            ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
            String encName = URLEncoder.encode(this.getName(), "UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[][] var7 = new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};
            int var8 = var7.length;

            for (byte[] bit : var7) {
                sha.update(bit);
            }
            String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
            String preventProxy = BungeeCord.getInstance().config.isPreventProxyConnections() && this.getSocketAddress() instanceof InetSocketAddress ? "&ip=" + URLEncoder.encode(this.getAddress().getAddress().getHostAddress(), "UTF-8") : "";
            String arg = String.format(AUTH_ARG, encName, encodedHash, preventProxy);

            FutureTask<String> task = new FutureTask<String>(()->{
                return null;
            });

            BUNGEE.getScheduler().runAsync(null, task);

            Callback<String> handler = new Callback<String>() {
                public void done(String result, Throwable error) {
                    if (error == null) {
                        LoginResult obj = BungeeCord.getInstance().gson.fromJson(result, LoginResult.class);
                        if (obj != null && obj.getId() != null) {
                            try {
                                LOGIN_REQUEST.set(vanHandle, obj);
                                NAME.set(vanHandle, obj.getName());
                                UNIQUE_ID.set(vanHandle, Util.getUUID(obj.getId()));
                                FINISH.invoke(vanHandle);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return;
                        }

                        disconnect(BUNGEE.getTranslation("offline_mode_player"));
                    } else {
                        disconnect(BUNGEE.getTranslation("mojang_fail"));
                        BUNGEE.getLogger().log(Level.SEVERE, "Error authenticating " + getName() + " with minecraft.net", error);
                    }

                }
            };





            //  HttpClient.get(authURL, ch.getHandle().eventLoop(), handler);
        }
    }
}
