package moe.caa.multilogin.bungee;

import com.google.common.base.Preconditions;
import io.netty.channel.EventLoop;
import moe.caa.multilogin.core.PluginData;
import moe.caa.multilogin.core.YggdrasilServiceSection;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.EncryptionUtil;
import net.md_5.bungee.Util;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.chat.BaseComponent;
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
import net.md_5.bungee.protocol.PacketWrapper;
import net.md_5.bungee.protocol.packet.EncryptionRequest;
import net.md_5.bungee.protocol.packet.EncryptionResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

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

            Map<Callback<String>, YggdrasilServiceSection> tasks = new Hashtable<>();
            AtomicReference<LoginResult> result = new AtomicReference<>();
            AtomicReference<YggdrasilServiceSection> ygg = new AtomicReference<>();
            AtomicBoolean down = new AtomicBoolean(false);

            if (PluginData.isOfficialYgg()) {
                String authURL = String.format("https://sessionserver.mojang.com/session/minecraft/%s", arg);
                Callback<String> call = new Callback<String>() {
                    @Override
                    public void done(String s, Throwable throwable) {
                        if(throwable == null){
                            LoginResult resultObj = BungeeCord.getInstance().gson.fromJson(s, LoginResult.class);
                            if(resultObj != null && resultObj.getId() != null){
                                ygg.set(tasks.get(this));
                                result.set(resultObj);
                            }
                        } else {
                            down.set(true);
                        }
                        tasks.remove(this);
                    }
                };
                tasks.put(call, YggdrasilServiceSection.OFFICIAL);
                HttpClient.get(authURL, ch.getHandle().eventLoop(), call);
            }
            for(YggdrasilServiceSection section : PluginData.getServiceSet()){
                String url = section.buildUrlStr(arg);
                Callback<String> call = new Callback<String>() {
                    @Override
                    public void done(String s, Throwable throwable) {
                        if(throwable == null){
                            LoginResult resultObj = BungeeCord.getInstance().gson.fromJson(s, LoginResult.class);
                            if(resultObj != null && resultObj.getId() != null){
                                ygg.set(tasks.get(this));
                                result.set(resultObj);
                            }
                        } else {
                            down.set(true);
                        }
                        tasks.remove(this);
                    }
                };
                tasks.put(call, section);
                HttpClient.get(url, ch.getHandle().eventLoop(), call);
            }

            BUNGEE.getScheduler().runAsync(MultiLogin.INSTANCE, ()->{
                long time = System.currentTimeMillis() + PluginData.getTimeOut();
                while(time > System.currentTimeMillis() && tasks.size() != 0){
                    if(result.get() != null){
                        try {
                            UUID onlineId = Util.getUUID(result.get().getId());
                            String text = PluginData.getUserVerificationMessage(onlineId, result.get().getName(), ygg.get());
                            if(text == null){
                                if (PluginData.isNoRepeatedName() && ygg.get().getPath().equalsIgnoreCase(PluginData.getSafeIdService())) {
                                    String name = result.get().getName();
                                    for (ProxiedPlayer player : BUNGEE.getPlayers()) {
                                        if (player.getName().equalsIgnoreCase(name)) {
                                            player.disconnect(new TextComponent(PluginData.getConfigurationConfig().getString("msgRushNameOnl")));
                                        }
                                    }
                                }
                                UUID swapUuid = PluginData.getSwapUUID(onlineId, ygg.get(), result.get().getName());
                                LOGIN_PROFILE.set(vanHandle, result.get());
                                UNIQUE_ID.set(vanHandle, swapUuid);
                                NAME.set(vanHandle, result.get().getName());
                                FINISH.invoke(vanHandle);
                                return;
                            } else {
                                vanHandle.disconnect(new TextComponent(text));
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            vanHandle.disconnect(new TextComponent(PluginData.getConfigurationConfig().getString("msgNoAdopt")));
                            return;
                        }
                    }
                }
                if(down.get()){
                    vanHandle.disconnect(BUNGEE.getTranslation("mojang_fail"));
                } else {
                    vanHandle.disconnect(BUNGEE.getTranslation("offline_mode_player"));
                }
            });
        }
    }
}
