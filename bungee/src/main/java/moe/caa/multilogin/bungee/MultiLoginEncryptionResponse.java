package moe.caa.multilogin.bungee;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import moe.caa.multilogin.bungee.impl.MultiLoginBungee;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.ReflectUtil;
import moe.caa.multilogin.core.auth.AuthErrorEnum;
import moe.caa.multilogin.core.auth.AuthResult;
import moe.caa.multilogin.core.auth.HttpAuth;
import moe.caa.multilogin.core.auth.VerificationResult;
import moe.caa.multilogin.core.data.PluginData;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.AbstractPacketHandler;
import net.md_5.bungee.protocol.ProtocolConstants;
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
import java.util.Arrays;
import java.util.UUID;

public class MultiLoginEncryptionResponse extends EncryptionResponse {
    private static Class<?> INITIAL_HANDLE_CLASS_STATE_CLASS;
    private static Field THIS_STATE;
    private static Field REQUEST;
    private static Field CHANNEL_WRAPPER;
    private static Field LOGIN_PROFILE;
    private static Field NAME;
    private static Field UNIQUE_ID;
    private static Method FINISH;


    public static void init() throws NoSuchFieldException, ClassNotFoundException {
        Class<InitialHandler> INITIAL_HANDLE_CLASS = InitialHandler.class;
        INITIAL_HANDLE_CLASS_STATE_CLASS = Class.forName("net.md_5.bungee.connection.InitialHandler$State");
        THIS_STATE = ReflectUtil.getField(INITIAL_HANDLE_CLASS, INITIAL_HANDLE_CLASS_STATE_CLASS);
        REQUEST = ReflectUtil.getField(INITIAL_HANDLE_CLASS, EncryptionRequest.class);
        CHANNEL_WRAPPER = ReflectUtil.getField(INITIAL_HANDLE_CLASS, ChannelWrapper.class);
        LOGIN_PROFILE = ReflectUtil.getField(INITIAL_HANDLE_CLASS, LoginResult.class);
        NAME = ReflectUtil.getField(INITIAL_HANDLE_CLASS, "name");
        UNIQUE_ID = ReflectUtil.getField(INITIAL_HANDLE_CLASS, "uniqueId");
        FINISH = ReflectUtil.getMethod(INITIAL_HANDLE_CLASS, "finish");
    }

    private byte[] sharedSecret;
    private byte[] verifyToken;

    public void read(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        this.sharedSecret = readArray(buf, 128);
        this.verifyToken = readArray(buf, 128);
    }

    public void write(ByteBuf buf, ProtocolConstants.Direction direction, int protocolVersion) {
        writeArray(this.sharedSecret, buf);
        writeArray(this.verifyToken, buf);
    }

    // handler.handle(this);
    public void handle(AbstractPacketHandler handler) throws Exception {
        if(handler instanceof InitialHandler){
            try {
                EncryptionRequest request = (EncryptionRequest) REQUEST.get(handler);
                ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.get(handler);
                Preconditions.checkState(THIS_STATE.get(handler) == ReflectUtil.getEnumIns(INITIAL_HANDLE_CLASS_STATE_CLASS, "ENCRYPT"), "Not expecting ENCRYPT");
                SecretKey sharedKey = EncryptionUtil.getSecret(this, request);
                if (sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length != 16) {
                    ch.close();
                } else {
                    BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
                    ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
                    BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
                    ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
                    String encName = URLEncoder.encode(((InitialHandler) handler).getName(), "UTF-8");
                    MessageDigest sha = MessageDigest.getInstance("SHA-1");
                    byte[][] var7 = new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()};

                    for (byte[] bit : var7) {
                        sha.update(bit);
                    }
                    String encodedHash = URLEncoder.encode((new BigInteger(sha.digest())).toString(16), "UTF-8");
                    String preventProxy = BungeeCord.getInstance().config.isPreventProxyConnections() && ((InitialHandler) handler).getSocketAddress() instanceof InetSocketAddress ? "&ip=" + URLEncoder.encode(((InitialHandler) handler).getAddress().getAddress().getHostAddress(), "UTF-8") : "";
                    String arg = String.format("hasJoined?username=%s&serverId=%s%s", encName, encodedHash, preventProxy);

                    BungeeCord.getInstance().getScheduler().runAsync(MultiLoginBungee.INSTANCE, ()->{
                        try {
                            AuthResult<LoginResult> result = HttpAuth.yggAuth(((InitialHandler) handler).getName(), arg, BungeeCord.getInstance().gson, LoginResult.class);
                            if(result.getErr() != null){
                                if(result.getErr() == AuthErrorEnum.SERVER_DOWN){
                                    ((InitialHandler) handler).disconnect(BungeeCord.getInstance().getTranslation("mojang_fail"));
                                } else {
                                    ((InitialHandler) handler).disconnect(BungeeCord.getInstance().getTranslation("offline_mode_player"));
                                }
                                return;
                            }
                            LoginResult loginResult = result.getResult();
                            UUID onlineId = Util.getUUID(loginResult.getId());
                            VerificationResult verificationResult = MultiCore.getUserVerificationMessage(onlineId, ((InitialHandler) handler).getName(), result.getYggdrasilService());
                            if(verificationResult.getFAIL_MSG() == null){
                                LOGIN_PROFILE.set(handler, new MultiLoginSignLoginResult(loginResult));
                                UNIQUE_ID.set(handler, verificationResult.getREDIRECT_UUID());
                                NAME.set(handler, loginResult.getName());
                                FINISH.invoke(handler);
                            } else {
                                ((InitialHandler) handler).disconnect(new TextComponent(verificationResult.getFAIL_MSG()));
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                            ((InitialHandler) handler).disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
                            MultiCore.getPlugin().getPluginLogger().severe("处理用户数据时出现异常");
                        }
                    });
                }
            } catch (Exception e){
                e.printStackTrace();
                ((InitialHandler) handler).disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
                MultiCore.getPlugin().getPluginLogger().severe("处理用户数据时出现异常");
            }

        } else {
            handler.handle(this);
        }
    }

    public byte[] getSharedSecret() {
        return this.sharedSecret;
    }

    public byte[] getVerifyToken() {
        return this.verifyToken;
    }

    public void setSharedSecret(byte[] sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public void setVerifyToken(byte[] verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String toString() {
        return "EncryptionResponse(sharedSecret=" + Arrays.toString(this.getSharedSecret()) + ", verifyToken=" + Arrays.toString(this.getVerifyToken()) + ")";
    }

    public MultiLoginEncryptionResponse() {
    }

    public MultiLoginEncryptionResponse(byte[] sharedSecret, byte[] verifyToken) {
        this.sharedSecret = sharedSecret;
        this.verifyToken = verifyToken;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof EncryptionResponse)) {
            return false;
        } else {
            MultiLoginEncryptionResponse other = (MultiLoginEncryptionResponse)o;
            if (!other.canEqual(this)) {
                return false;
            } else if (!Arrays.equals(this.getSharedSecret(), other.getSharedSecret())) {
                return false;
            } else {
                return Arrays.equals(this.getVerifyToken(), other.getVerifyToken());
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof MultiLoginEncryptionResponse;
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + Arrays.hashCode(this.getSharedSecret());
        result = result * 59 + Arrays.hashCode(this.getVerifyToken());
        return result;
    }
}
