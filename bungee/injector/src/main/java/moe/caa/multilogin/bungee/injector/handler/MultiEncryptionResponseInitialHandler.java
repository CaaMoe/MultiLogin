package moe.caa.multilogin.bungee.injector.handler;

import com.google.common.base.Preconditions;
import moe.caa.multilogin.api.auth.AuthResult;
import moe.caa.multilogin.api.auth.GameProfile;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;
import net.md_5.bungee.jni.cipher.BungeeCipher;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.netty.cipher.CipherDecoder;
import net.md_5.bungee.netty.cipher.CipherEncoder;
import net.md_5.bungee.protocol.Property;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import net.md_5.bungee.protocol.packet.LoginRequest;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

// EncryptionResponse 这个包
public class MultiEncryptionResponseInitialHandler extends AbstractMultiInitialHandler<EncryptionResponse> {
    // 运行时产生数据
    private Enum<?> thisState = null;
    private LoginRequest loginRequest;
    private EncryptionRequest request;
    private ChannelWrapper ch;

    public MultiEncryptionResponseInitialHandler(InitialHandler initialHandler, MultiCoreAPI multiCoreAPI) {
        super(initialHandler, multiCoreAPI);
    }

    private void initValue() throws Throwable {
        thisState = (Enum<?>) thisStateFieldGetter.invoke(initialHandler);
        loginRequest = (LoginRequest) loginRequestFieldGetter.invoke(initialHandler);
        request = (EncryptionRequest) requestFieldGetter.invoke(initialHandler);
        ch = (ChannelWrapper) chFieldGetter.invoke(initialHandler);
    }

    @Override
    public void handle(EncryptionResponse encryptResponse) throws Throwable {
        initValue();

        /*
         * Bungee 的方法
         */
        Preconditions.checkState(thisState == state$ENCRYPT, "Not expecting ENCRYPT");
        Preconditions.checkState(EncryptionUtil.check(loginRequest.getPublicKey(), encryptResponse, request), "Invalid verification");

        SecretKey sharedKey = EncryptionUtil.getSecret(encryptResponse, request);
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_DECODER, PipelineUtils.DECRYPT_HANDLER, new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore(PipelineUtils.FRAME_PREPENDER, PipelineUtils.ENCRYPT_HANDLER, new CipherEncoder(encrypt));

        String encName = URLEncoder.encode((String) getNameMethod.invoke(initialHandler), StandardCharsets.UTF_8);

        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][]{
                request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()
        }) {
            sha.update(bit);
        }
        String encodedHash = URLEncoder.encode(new BigInteger(sha.digest()).toString(16), StandardCharsets.UTF_8);

        /*
         * 这里是我们的了
         */
        String ip = getSocketAddressMethod.invoke(initialHandler) instanceof InetSocketAddress ? URLEncoder.encode(((InetSocketAddress) getAddressMethod.invoke(initialHandler)).getAddress().getHostAddress(), StandardCharsets.UTF_8) : "";

        multiCoreAPI.getPlugin().getRunServer().getScheduler().runTaskAsync(() -> {

            try {
                AuthResult authResult = multiCoreAPI.getAuthHandler().auth(encName, encodedHash, ip);
                if (authResult.isAllowed()) {
                    GameProfile response = authResult.getResponse();
                    loginProfileFieldSetter.invoke(initialHandler, generateGameProfile(response));
                    nameFieldSetter.invoke(initialHandler, response.getName());
                    uniqueIdFieldSetter.invoke(initialHandler, response.getId());
                    finishMethod.invoke(initialHandler);
                } else {
                    initialHandler.disconnect(authResult.getKickMessage());
                }
            } catch (Throwable e) {
                initialHandler.disconnect(new TextComponent(multiCoreAPI.getLanguageHandler().getMessage("auth_error")));
                LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
            }

        });

        // This is ok.
        // thisState = InitialHandler.State.FINISHING;
    }

    private LoginResult generateGameProfile(GameProfile response) {
        return new LoginResult(
                response.getId().toString().replace("-", ""),
                response.getName(),
                response.getPropertyMap().values().stream().map(s -> {
                    return new Property(s.getName(), s.getValue(), s.getSignature());
                }).toArray(Property[]::new)
        );
    }
}
