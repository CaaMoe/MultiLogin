package moe.caa.multilogin.paper.internal.channel;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.papermc.paper.adventure.PaperAdventure;
import moe.caa.multilogin.common.internal.online.OnlineData;
import moe.caa.multilogin.common.internal.profile.ProfileManager;
import moe.caa.multilogin.common.internal.user.UserManager;
import moe.caa.multilogin.common.internal.util.ReflectUtil;
import moe.caa.multilogin.paper.internal.main.MultiLoginPaperMain;
import net.kyori.adventure.text.Component;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.login.ClientboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.login.ServerboundKeyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.StringUtil;
import org.apache.commons.lang3.Validate;

import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiLoginLoginPhasePacketHandler extends SimpleChannelInboundHandler<Packet<?>> {
    private static MethodHandle listenerGetServer;
    private static MethodHandle listenerGetChallenger;
    private static MethodHandle listenerCallCallPlayerPreLoginEvent;
    private static MethodHandle listenerCallStartClientVerification;

    private final MultiLoginPaperMain paperMain;

    private final AtomicBoolean handledHelloPacket = new AtomicBoolean(false);
    private final AtomicBoolean handledKeyPacket = new AtomicBoolean(false);

    private boolean requiredAuthenticate = false; // todo 快速测试设置

    // 以下字段在 HelloPacket 初始化
    private ServerLoginPacketListenerImpl serverLoginPacketListener;
    private MinecraftServer server;
    private byte[] challenge;

    public MultiLoginLoginPhasePacketHandler(MultiLoginPaperMain paperMain) {
        this.paperMain = paperMain;
    }

    public static void init() throws Exception {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        MultiLoginLoginPhasePacketHandler.listenerGetServer = lookup.unreflectGetter(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredField("server")));
        MultiLoginLoginPhasePacketHandler.listenerGetChallenger = lookup.unreflectGetter(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredField("challenge")));
        MultiLoginLoginPhasePacketHandler.listenerCallCallPlayerPreLoginEvent = lookup.unreflect(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredMethod("callPlayerPreLoginEvents", GameProfile.class)));
        MultiLoginLoginPhasePacketHandler.listenerCallStartClientVerification = lookup.unreflect(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredMethod("startClientVerification", GameProfile.class)));
    }

    private void disconnect(Component component) {
        serverLoginPacketListener.disconnect(PaperAdventure.asVanilla(component));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) {
        if (!ctx.channel().isOpen()) return;

        if (msg instanceof ServerboundKeyPacket packet) {
            try {
                channelReadKeyPacket(ctx, packet);
            } catch (Throwable t) {
                paperMain.getPlatformLogger().error("Failed to process key packet", t);
                disconnect(paperMain.getCore().messageConfig.loginUnknownError.get());
            }
        } else if (msg instanceof ServerboundHelloPacket packet) {
            try {
                channelReadHelloPacket(ctx, packet);
            } catch (Throwable t) {
                paperMain.getPlatformLogger().error("Failed to process hello packet", t);
                disconnect(paperMain.getCore().messageConfig.loginUnknownError.get());
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void channelReadHelloPacket(ChannelHandlerContext ctx, ServerboundHelloPacket packet) throws Throwable {
        if (handledHelloPacket.getAndSet(true)) throw new IllegalStateException("Already handled hello packet");
        serverLoginPacketListener = (ServerLoginPacketListenerImpl) ((Connection) ctx.pipeline().get(ChannelInjector.MINECRAFT_PACKET_HANDLER_NAME)).getPacketListener();
        server = (MinecraftServer) listenerGetServer.invoke(serverLoginPacketListener);
        challenge = (byte[]) listenerGetChallenger.invoke(serverLoginPacketListener);

        Validate.validState(serverLoginPacketListener.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");
        if (!StringUtil.isReasonablePlayerName(packet.name())) {
            paperMain.getPlatformLogger().warn("Player tried to login with invalid characters in name: " + packet.name());
            disconnect(paperMain.getCore().messageConfig.loginInvalidCharactersInName.get());
        }

        serverLoginPacketListener.requestedUuid = packet.profileId();
        serverLoginPacketListener.requestedUsername = packet.name();

        // 强制专用服务器模式, 强制进行加密连接
        serverLoginPacketListener.state = ServerLoginPacketListenerImpl.State.KEY;
        serverLoginPacketListener.connection.send(new ClientboundHelloPacket("", server.getKeyPair().getPublic().getEncoded(), challenge, requiredAuthenticate));
    }

    private String setupCrypt(ServerboundKeyPacket packet) {
        String serverID;
        try {
            PrivateKey _private = server.getKeyPair().getPrivate();
            if (!packet.isChallengeValid(challenge, _private)) {
                throw new IllegalStateException("Invalid challenge!");
            }

            SecretKey secretKey = packet.getSecretKey(_private);
            serverID = (new BigInteger(Crypt.digestData("", server.getKeyPair().getPublic(), secretKey))).toString(16);
            serverLoginPacketListener.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            serverLoginPacketListener.connection.setEncryptionKey(secretKey);
            return serverID;
        } catch (CryptException e) {
            throw new IllegalStateException("Failed to encrypt connection.", e);
        }
    }

    private void channelReadKeyPacket(ChannelHandlerContext ctx, ServerboundKeyPacket packet) throws Throwable {
        if (handledKeyPacket.getAndSet(true)) throw new IllegalStateException("Already handled key packet");

        // 模拟正常操作
        Validate.validState(serverLoginPacketListener.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");
        SocketAddress remoteAddress = serverLoginPacketListener.connection.getRemoteAddress();

        String serverID = setupCrypt(packet);
        String username = Objects.requireNonNull(serverLoginPacketListener.requestedUsername, "Player name not initialized");
        InetAddress playerIP = server.getPreventProxyConnections() && remoteAddress instanceof InetSocketAddress ? ((InetSocketAddress) remoteAddress).getAddress() : null;

        paperMain.getCore().asyncExecutor.execute(() -> processYggdrasilAuthenticate(serverID, username, playerIP));
    }

    private void handleCreateProfileFailedResult(ProfileManager.CreateProfileResult.CreateProfileFailedResult result) {
        Component disconnectReason = switch (result) {
            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseReasonResult enumResult ->
                    switch (enumResult.reason) {
                        case UUID_CONFLICT -> paperMain.getCore().messageConfig.loginProfileCreateUuidConflict.get();
                        case NAME_CONFLICT -> paperMain.getCore().messageConfig.loginProfileCreateNameConflict.get();
                        case NAME_AMEND_RESTRICT ->
                                paperMain.getCore().messageConfig.loginProfileCreateNameAmendRestrict.get();
                    };
            case ProfileManager.CreateProfileResult.CreateProfileFailedResult.CreateProfileFailedBecauseThrowResult throwResult -> {
                paperMain.getPlatformLogger().error("Failed to create profile during user creation.", throwResult.throwable);
                yield paperMain.getCore().messageConfig.loginUnknownError.get();
            }
        };
        disconnect(disconnectReason);
    }


    private void handleGetUserSucceedResult(UserManager.GetUserResult.GetUserSucceedResult result, GameProfile gameProfile) throws Throwable {
        ProfileManager.Profile profile = null;

        // 一次性登录
        Optional<Integer> oneTimeLoginProfileID = paperMain.getCore().userManager.getOneTimeLoginProfileIDByUserID(result.user.userID());
        if (oneTimeLoginProfileID.isPresent()) {
            paperMain.getPlatformLogger().info("User " + result.user.getDisplayName() + " is using one-time login profile ID " + oneTimeLoginProfileID.get());
            profile = paperMain.getCore().profileManager.getProfileSnapshotByID(oneTimeLoginProfileID.get());

            if (profile == null) {
                paperMain.getPlatformLogger().error("User " + result.user.getDisplayName() + " attempted to use one-time login profile ID " + oneTimeLoginProfileID.get() + " which does not exist. Falling back to their selected profile ID " + result.user.selectProfileID() + ".");
            }
        }


        if (profile == null) {
            Optional<Integer> selectedProfile = result.user.selectProfileID();
            // 登录到默认档案
            if (selectedProfile.isPresent()) {
                profile = paperMain.getCore().profileManager.getProfileSnapshotByID(selectedProfile.get());
            } else {
                // 没有选择档案
                // 看看有没有已拥有的档案
                List<Integer> avaliableProfileIDList = paperMain.getCore().userManager.getAvailableProfileIDListByUserID(result.user.userID());
                if (!avaliableProfileIDList.isEmpty()) {
                    paperMain.getPlatformLogger().warn("User " + result.user.getDisplayName() + " has no selected profile and will choose from their available profiles.");
                    for (Integer profileID : avaliableProfileIDList) {
                        profile = paperMain.getCore().profileManager.getProfileSnapshotByID(profileID);
                        if (profile != null) {
                            // set selected profile todo
                            break;
                        }
                    }
                }
            }

            if (profile == null) {
                paperMain.getPlatformLogger().warn("User " + result.user.getDisplayName() + " has no selected profile and no available profiles, Creating new profile...");

                ProfileManager.CreateProfileResult profileCreateResult = paperMain.getCore().profileManager.createProfile(
                        result.user.userUUID(),
                        result.user.username(),
                        ProfileManager.AmendRuleUUID.RANDOM,
                        ProfileManager.AmendRuleName.INCREMENT_NUMBER_AND_RIGHT_TRUNCATE
                );
                switch (profileCreateResult) {
                    case ProfileManager.CreateProfileResult.CreateProfileFailedResult createProfileFailedResult -> {
                        handleCreateProfileFailedResult(createProfileFailedResult);
                        return;
                    }
                    case ProfileManager.CreateProfileResult.CreateProfileSucceedResult createProfileSucceedResult -> {
                        profile = createProfileSucceedResult.profile;
                        // todo 写入 selectedProfile 和 have profile
                    }
                }
            }
        }

        OnlineData data = new OnlineData(
                new OnlineData.OnlineUser(result.user.userID(), "official", Component.text("测试登录"), result.user.userUUID(), result.user.username()),
                new OnlineData.OnlineProfile(profile.profileID(), profile.profileUUID(), profile.profileName())
        );
        paperMain.getOnlinePlayerManager().putOnlineData(serverLoginPacketListener.connection, data);

        gameProfile = (GameProfile) listenerCallCallPlayerPreLoginEvent.invoke(serverLoginPacketListener, gameProfile);

        if (!gameProfile.getId().equals(data.onlineProfile().profileUUID()) || !gameProfile.getName().equals(data.onlineProfile().profileName())) {
            paperMain.getPlatformLogger().warn("Check your plugin list as players triggered profile changes during the PreLoginEvent. (expected: " + data.onlineProfile().profileUUID() + "/" + data.onlineProfile().profileName() + ", got: " + gameProfile.getId() + "/" + gameProfile.getName() + ")");
            disconnect(paperMain.getCore().messageConfig.loginUnknownError.get());
            return;
        }

        paperMain.getPlatformLogger().info("User " + result.user.getDisplayName() + " logged in with profile " + profile.getDisplayName());
        listenerCallStartClientVerification.invoke(serverLoginPacketListener, gameProfile);
    }


    private void processYggdrasilAuthenticate(String serverID, String username, InetAddress playerIP) {
        try {
            // todo test bypass
            GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes(new byte[]{0, 0, 0, 0, 0, 0}), username);
            String loginMethod = "official";
            UserManager.GetUserResult userResult = paperMain.getCore().userManager.getOrCreateUser(loginMethod, gameProfile.getId(), gameProfile.getName());
            switch (userResult) {
                case UserManager.GetUserResult.GetUserSucceedResult result -> {
                    handleGetUserSucceedResult(result, gameProfile);
                }
                case UserManager.GetUserResult.GetUserFailedResult result -> {
                    paperMain.getPlatformLogger().error("Failed to get user data.", result.throwable);
                    disconnect(paperMain.getCore().messageConfig.loginUnknownError.get());
                }
            }
        } catch (Throwable t) {
            paperMain.getPlatformLogger().error("Failed to authenticate player " + username, t);
            disconnect(paperMain.getCore().messageConfig.loginUnknownError.get());
        }
    }
}
