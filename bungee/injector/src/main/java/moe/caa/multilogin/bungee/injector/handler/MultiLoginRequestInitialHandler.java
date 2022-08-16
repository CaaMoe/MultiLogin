package moe.caa.multilogin.bungee.injector.handler;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import moe.caa.multilogin.api.logger.LoggerProvider;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.PlayerPublicKey;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.LoginRequest;
import net.md_5.bungee.util.AllowedCharacters;

import java.time.Instant;
import java.util.UUID;

// LoginRequest 这个包
public class MultiLoginRequestInitialHandler extends AbstractMultiInitialHandler<LoginRequest> {
    private Enum<?> thisState;
    private LoginRequest loginRequest;
    @Getter(value = AccessLevel.PRIVATE)
    private boolean onlineMode;
    private BungeeCord bungee;
    private ChannelWrapper ch;
    @Getter(value = AccessLevel.PRIVATE)
    private UUID uniqueId;

    public MultiLoginRequestInitialHandler(InitialHandler initialHandler, MultiCoreAPI multiCoreAPI) {
        super(initialHandler, multiCoreAPI);
    }

    private void initValues() throws Throwable {
        this.loginRequest = (LoginRequest) loginRequestFieldGetter.invoke(initialHandler);
        this.thisState = (Enum<?>) thisStateFieldGetter.invoke(initialHandler);
        this.uniqueId = (UUID) uniqueIdFieldGetter.invoke(initialHandler);
        this.ch = (ChannelWrapper) chFieldGetter.invoke(initialHandler);
        this.onlineMode = (boolean) onlineModeFieldGetter.invoke(initialHandler);
        this.bungee = (BungeeCord) bungeeFieldGetter.invoke(initialHandler);
    }

    @Override
    public void handle(LoginRequest packet) throws Throwable {
        initValues();

        Preconditions.checkState(thisState == state$USERNAME, "Not expecting USERNAME");

        if (!AllowedCharacters.isValidName(packet.getData(), onlineMode)) {
            initialHandler.disconnect(bungee.getTranslation("name_invalid"));
            return;
        }

        if (BungeeCord.getInstance().config.isEnforceSecureProfile()) {
            PlayerPublicKey publicKey = packet.getPublicKey();
            if (publicKey == null) {
                initialHandler.disconnect(bungee.getTranslation("secure_profile_required"));
                return;
            }

            if (Instant.ofEpochMilli(publicKey.getExpiry()).isBefore(Instant.now())) {
                initialHandler.disconnect(bungee.getTranslation("secure_profile_expired"));
                return;
            }

            // 签名验证
//            if ((int)getVersionMethod.invoke(initialHandler) < ProtocolConstants.MINECRAFT_1_19_1) {
//                if (!EncryptionUtil.check(publicKey, null)) {
//                    initialHandler.disconnect(bungee.getTranslation("secure_profile_invalid"));
//                    return;
//                }
//            }
        }

        loginRequestFieldSetter.invoke(initialHandler, packet);
        this.loginRequest = packet;

        int limit = BungeeCord.getInstance().config.getPlayerLimit();
        if (limit > 0 && bungee.getOnlineCount() >= limit) {
            initialHandler.disconnect(bungee.getTranslation("proxy_full"));
            return;
        }

        // If offline mode and they are already on, don't allow to connect
        // We can just check by UUID here as names are based on UUID
        if (!isOnlineMode() && bungee.getPlayer(getUniqueId()) != null) {
            initialHandler.disconnect(bungee.getTranslation("already_connected_proxy"));
            return;
        }

        Callback<PreLoginEvent> callback = (result, error) -> {
            if (result.isCancelled()) {
                BaseComponent[] reason = result.getCancelReasonComponents();
                initialHandler.disconnect((reason != null) ? reason : TextComponent.fromLegacyText(bungee.getTranslation("kick_message")));
                return;
            }
            if (ch.isClosed()) {
                return;
            }

            try {
                if (onlineMode) {
                    thisStateFieldSetter.invoke(initialHandler, state$ENCRYPT);

                    EncryptionRequest request = EncryptionUtil.encryptRequest();
                    requestFieldSetter.invoke(initialHandler, request);
                    unsafe$sendPacketMethod.invoke(unsafeFieldGetter.invoke(initialHandler), request);
                } else {
                    thisStateFieldSetter.invoke(initialHandler, state$FINISHING);
                    finishMethod.invoke(initialHandler);
                }
            } catch (Throwable e) {
                initialHandler.disconnect(new TextComponent(multiCoreAPI.getLanguageHandler().getMessage("auth_error")));
                LoggerProvider.getLogger().error("An exception occurred while processing a login request.", e);
            }
        };

        // fire pre login event
        bungee.getPluginManager().callEvent(new PreLoginEvent(initialHandler, callback));
    }


}
