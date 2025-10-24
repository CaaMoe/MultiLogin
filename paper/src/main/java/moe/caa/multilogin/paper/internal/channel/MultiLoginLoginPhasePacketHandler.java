package moe.caa.multilogin.paper.internal.channel;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.connection.PaperPlayerLoginConnection;
import moe.caa.multilogin.common.internal.data.LoggingUser;
import moe.caa.multilogin.common.internal.data.OnlineData;
import moe.caa.multilogin.common.internal.util.Key;
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
import org.apache.commons.lang3.Validate;
import org.bukkit.NamespacedKey;

import javax.crypto.SecretKey;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiLoginLoginPhasePacketHandler extends SimpleChannelInboundHandler<Packet<?>> implements LoggingUser {
    private static MethodHandle listenerGetServer;
    private static MethodHandle listenerGetChallenger;
    private static MethodHandle listenerGetPaperLoginConnection;
    private static MethodHandle listenerCallCallPlayerPreLoginEvent;
    private static MethodHandle listenerCallStartClientVerification;

    private final MultiLoginPaperMain paperMain;

    private final AtomicBoolean handledHelloPacket = new AtomicBoolean(false);
    private final AtomicBoolean handledKeyPacket = new AtomicBoolean(false);
    private final CompletableFuture<SwitchToEncryptedResult> switchToEncryptedResultCompletableFuture = new CompletableFuture<>();

    // 以下字段在 HelloPacket 初始化
    private PaperPlayerLoginConnection paperLoginConnection;
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
        MultiLoginLoginPhasePacketHandler.listenerGetPaperLoginConnection = lookup.unreflectGetter(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredField("paperLoginConnection")));
        MultiLoginLoginPhasePacketHandler.listenerCallCallPlayerPreLoginEvent = lookup.unreflect(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredMethod("callPlayerPreLoginEvents", GameProfile.class)));
        MultiLoginLoginPhasePacketHandler.listenerCallStartClientVerification = lookup.unreflect(ReflectUtil.openAccess(ServerLoginPacketListenerImpl.class.getDeclaredMethod("startClientVerification", GameProfile.class)));
    }

    @Override
    public UUID getExpectUUID() {
        return serverLoginPacketListener.requestedUuid;
    }

    @Override
    public String getExpectUsername() {
        return serverLoginPacketListener.requestedUsername;
    }

    @Override
    public InetAddress getPlayerIP() {
        SocketAddress remoteAddress = serverLoginPacketListener.connection.getRemoteAddress();
        return server.getPreventProxyConnections() && remoteAddress instanceof InetSocketAddress ? ((InetSocketAddress) remoteAddress).getAddress() : null;
    }

    @Override
    public boolean isTransferred() {
        return serverLoginPacketListener.transferred;
    }

    @Override
    public byte[] requestCookie(Key cookieKey) throws Exception {
        return paperLoginConnection.retrieveCookie(new NamespacedKey(cookieKey.namespace(), cookieKey.key())).get();
    }

    @Override
    public void closeConnect(Component component) {
        serverLoginPacketListener.disconnect(PaperAdventure.asVanilla(component));
    }

    @Override
    public void closeConnection() {
        serverLoginPacketListener.connection.channel.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) {
        if (!ctx.channel().isOpen()) return;

        if (msg instanceof ServerboundKeyPacket packet) {
            try {
                channelReadKeyPacket(ctx, packet);
            } catch (Throwable t) {
                paperMain.getPlatformLogger().error("Failed to process key packet", t);
                closeConnect(paperMain.getCore().messageConfig.loginUnknownError.get());
            }
        } else if (msg instanceof ServerboundHelloPacket packet) {
            try {
                channelReadHelloPacket(ctx, packet);
            } catch (Throwable t) {
                paperMain.getPlatformLogger().error("Failed to process hello packet", t);
                closeConnect(paperMain.getCore().messageConfig.loginUnknownError.get());
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
        paperLoginConnection = (PaperPlayerLoginConnection) listenerGetPaperLoginConnection.invoke(serverLoginPacketListener);
        serverLoginPacketListener.requestedUuid = packet.profileId();
        serverLoginPacketListener.requestedUsername = packet.name();

        Validate.validState(serverLoginPacketListener.state == ServerLoginPacketListenerImpl.State.HELLO, "Unexpected hello packet");

        attachActivityCheck();
        paperMain.getCore().virtualPerTaskExecutor.execute(() -> paperMain.getCore().loginManager.handleLogging(this));
    }

    private void attachActivityCheck() {
        paperMain.getCore().virtualPerTaskExecutor.execute(() -> {
            try {
                while (true) {
                    TimeUnit.MILLISECONDS.sleep(100);
                    if (!serverLoginPacketListener.connection.channel.isOpen()) {
                        switchToEncryptedResultCompletableFuture.complete(new SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedReasonResult(SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedReasonResult.Reason.CLOSED));
                        return;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }


    @Override
    public SwitchToEncryptedResult switchToEncryptedState(boolean shouldClientAuthenticate) throws Exception {
        serverLoginPacketListener.state = ServerLoginPacketListenerImpl.State.KEY;
        serverLoginPacketListener.connection.send(new ClientboundHelloPacket("", server.getKeyPair().getPublic().getEncoded(), challenge, shouldClientAuthenticate));

        return switchToEncryptedResultCompletableFuture.get();
    }

    private void channelReadKeyPacket(ChannelHandlerContext ctx, ServerboundKeyPacket packet) {
        if (handledKeyPacket.getAndSet(true)) throw new IllegalStateException("Already handled key packet");
        Validate.validState(serverLoginPacketListener.state == ServerLoginPacketListenerImpl.State.KEY, "Unexpected key packet");

        try {
            PrivateKey _private = server.getKeyPair().getPrivate();
            if (!packet.isChallengeValid(challenge, _private)) {
                throw new IllegalStateException("Invalid challenge!");
            }

            SecretKey secretKey = packet.getSecretKey(_private);
            String serverID = (new BigInteger(Crypt.digestData("", server.getKeyPair().getPublic(), secretKey))).toString(16);
            serverLoginPacketListener.state = ServerLoginPacketListenerImpl.State.AUTHENTICATING;
            serverLoginPacketListener.connection.setEncryptionKey(secretKey);

            switchToEncryptedResultCompletableFuture.complete(new SwitchToEncryptedResult.SwitchToEncryptedSucceedResult(serverID));
        } catch (Throwable t) {
            switchToEncryptedResultCompletableFuture.complete(new SwitchToEncryptedResult.SwitchToEncryptedFailedResult.SwitchToEncryptedFailedThrowResult(t));
        }
    }

    @Override
    public void completeLogin(OnlineData data) throws Throwable {
        paperMain.getOnlinePlayerManager().putOnlineData(serverLoginPacketListener.connection, data);


        GameProfile gameProfile = new GameProfile(data.onlineUser().profile().uuid(), data.onlineUser().profile().username());
        for (moe.caa.multilogin.common.internal.data.GameProfile.Property property : data.onlineUser().profile().properties()) {
            gameProfile.getProperties().put(property.name(), new Property(property.name(), property.value(), property.signature()));
        }

        gameProfile = (GameProfile) listenerCallCallPlayerPreLoginEvent.invoke(serverLoginPacketListener, gameProfile);

        if (!gameProfile.getId().equals(data.onlineUser().profile().uuid()) || !gameProfile.getName().equals(data.onlineUser().profile().username())) {
            paperMain.getPlatformLogger().warn("Check your plugin list as players triggered profile changes during the PreLoginEvent. (expected: " + data.onlineUser().profile().username() + "/" + data.onlineUser().profile().username() + ", got: " + gameProfile.getId() + "/" + gameProfile.getName() + ")");
            closeConnect(paperMain.getCore().messageConfig.loginUnknownError.get());
            return;
        }

        listenerCallStartClientVerification.invoke(serverLoginPacketListener, gameProfile);
    }
}