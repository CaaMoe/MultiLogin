package moe.caa.multilogin.bungee;

import com.google.common.base.Preconditions;
import moe.caa.multilogin.core.PluginData;
import moe.caa.multilogin.core.YggdrasilService;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.http.HttpClient;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MultiInitialHandler extends InitialHandler{
    private static final String AUTH_ARG = "hasJoined?username=%s&serverId=%s%s";
    private final Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
    private final Class INITIAL_HANDLE_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
    private final Field THIS_STATE = RefUtil.getField(INITIAL_HANDLE_CLASS, INITIAL_HANDLE_CLASS_STATE_CLASS);
    private final Field REQUEST = RefUtil.getField(INITIAL_HANDLE_CLASS, EncryptionRequest.class);
    private final Field CHANNEL_WRAPPER = RefUtil.getField(INITIAL_HANDLE_CLASS, ChannelWrapper.class);
    private final Field LOGIN_PROFILE = RefUtil.getField(INITIAL_HANDLE_CLASS, LoginResult.class);
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
            String encName = URLEncoder.encode(vanHandle.getName(), "UTF-8");
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[][] var7 = new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};

            for (byte[] bit : var7) {
                sha.update(bit);
            }
            String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
            String preventProxy = BungeeCord.getInstance().config.isPreventProxyConnections() && vanHandle.getSocketAddress() instanceof InetSocketAddress ? "&ip=" + URLEncoder.encode(vanHandle.getAddress().getAddress().getHostAddress(), "UTF-8") : "";
            String arg = String.format("hasJoined?username=%s&serverId=%s%s", encName, encodedHash, preventProxy);

            BUNGEE.getScheduler().runAsync(MultiLogin.INSTANCE, ()->{
                YggdrasilService.AuthResult<LoginResult> result = YggdrasilService.yggAuth(arg, BUNGEE.gson, LoginResult.class);
                if(result.getErr() != null){
                    if(result.getErr() == YggdrasilService.AuthErrorEnum.SERVER_DOWN){
                        vanHandle.disconnect(BUNGEE.getTranslation("mojang_fail"));
                    } else {
                        vanHandle.disconnect(BUNGEE.getTranslation("offline_mode_player"));
                    }
                    return;
                }
                LoginResult loginResult = result.getResult();

                try {
                    UUID onlineId = Util.getUUID(loginResult.getId());
                    String text = PluginData.getUserVerificationMessage(onlineId, loginResult.getName(), result.getYggdrasilService());
                    if(text == null){
                        if (PluginData.isNoRepeatedName() && result.getYggdrasilService().getPath().equalsIgnoreCase(PluginData.getSafeIdService())) {
                            String name = loginResult.getName();
                            for (ProxiedPlayer player : BUNGEE.getPlayers()) {
                                if (player.getName().equalsIgnoreCase(name)) {
                                    player.disconnect(new TextComponent(PluginData.getConfigurationConfig().getString("msgRushNameOnl")));
                                }
                            }
                        }
                        UUID swapUuid = PluginData.getSwapUUID(onlineId, result.getYggdrasilService(), loginResult.getName());
                        LOGIN_PROFILE.set(vanHandle, loginResult);
                        UNIQUE_ID.set(vanHandle, swapUuid);
                        NAME.set(vanHandle, loginResult);
                        FINISH.invoke(vanHandle);
                    } else {
                        vanHandle.disconnect(new TextComponent(text));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    vanHandle.disconnect(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
                }
            });
        }
    }
}
