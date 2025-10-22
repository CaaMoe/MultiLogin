package moe.caa.multilogin.paper.internal.channel;

import com.mojang.authlib.GameProfile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.papermc.paper.adventure.PaperAdventure;
import moe.caa.multilogin.common.internal.online.OnlineData;
import moe.caa.multilogin.common.internal.online.OnlineProfile;
import moe.caa.multilogin.common.internal.online.OnlineUser;
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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiLoginLoginPhasePacketHandler extends SimpleChannelInboundHandler<Packet<?>> {
    private static MethodHandle listenerGetServer;
    private static MethodHandle listenerGetChallenger;
    private static MethodHandle listenerCallCallPlayerPreLoginEvent;
    private static MethodHandle listenerCallStartClientVerification;

    private final MultiLoginPaperMain multiLoginPaperMain;

    private final AtomicBoolean handledHelloPacket = new AtomicBoolean(false);
    private final AtomicBoolean handledKeyPacket = new AtomicBoolean(false);

    private boolean requiredAuthenticate = false; // todo 快速测试设置

    // 以下字段在 HelloPacket 初始化
    private ServerLoginPacketListenerImpl serverLoginPacketListener;
    private MinecraftServer server;
    private byte[] challenge;

    public MultiLoginLoginPhasePacketHandler(MultiLoginPaperMain multiLoginPaperMain) {
        this.multiLoginPaperMain = multiLoginPaperMain;
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
                multiLoginPaperMain.getPlatformLogger().error("Failed to process key packet", t);
                disconnect(multiLoginPaperMain.getCore().messageConfig.loginUnknownError.get());
            }
        } else if (msg instanceof ServerboundHelloPacket packet) {
            try {
                channelReadHelloPacket(ctx, packet);
            } catch (Throwable t) {
                multiLoginPaperMain.getPlatformLogger().error("Failed to process hello packet", t);
                disconnect(multiLoginPaperMain.getCore().messageConfig.loginUnknownError.get());
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
            multiLoginPaperMain.getPlatformLogger().warn("Player tried to login with invalid characters in name: " + packet.name());
            disconnect(multiLoginPaperMain.getCore().messageConfig.loginInvalidCharactersInName.get());
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

        multiLoginPaperMain.getCore().asyncExecutor.execute(() -> processYggdrasilAuthenticate(serverID, username, playerIP));
    }

    private void processYggdrasilAuthenticate(String serverID, String username, InetAddress playerIP) {
        try {
            // todo test bypass
            GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes(new byte[]{0, 0, 0, 0, 0, 0}), username);

            OnlineData data = new OnlineData(
                    new OnlineUser(23, "official", Component.text("测试登录"), UUID.randomUUID(), "testUser"),
                    new OnlineProfile(59, gameProfile.getId(), gameProfile.getName())
            );
            multiLoginPaperMain.getOnlinePlayerManager().putOnlineData(serverLoginPacketListener.connection, data);

            gameProfile = (GameProfile) listenerCallCallPlayerPreLoginEvent.invoke(serverLoginPacketListener, gameProfile);

            if (!gameProfile.getId().equals(data.onlineProfile().profileUUID()) || !gameProfile.getName().equals(data.onlineProfile().profileName())) {
                multiLoginPaperMain.getPlatformLogger().warn("Check your plugin list as players triggered profile changes during the PreLoginEvent. (expected: " + data.onlineProfile().profileUUID() + "/" + data.onlineProfile().profileName() + ", got: " + gameProfile.getId() + "/" + gameProfile.getName() + ")");
                disconnect(multiLoginPaperMain.getCore().messageConfig.loginUnknownError.get());
                return;
            }

            multiLoginPaperMain.getPlatformLogger().info("UUID of player " + gameProfile.getName() + " is " + gameProfile.getId());
            listenerCallStartClientVerification.invoke(serverLoginPacketListener, gameProfile);
        } catch (Throwable t) {
            multiLoginPaperMain.getPlatformLogger().error("Failed to authenticate player " + username, t);
            disconnect(multiLoginPaperMain.getCore().messageConfig.loginUnknownError.get());
        }
    }
}
