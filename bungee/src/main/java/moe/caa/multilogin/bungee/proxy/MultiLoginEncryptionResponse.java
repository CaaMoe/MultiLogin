/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.bungee.proxy.MultiLoginEncryptionResponse
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.bungee.proxy;

import com.google.common.base.Preconditions;
import moe.caa.multilogin.bungee.impl.MultiLoginBungee;
import moe.caa.multilogin.bungee.task.AuthTask;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.data.data.PluginData;
import moe.caa.multilogin.core.util.I18n;
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
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MultiLoginEncryptionResponse extends EncryptionResponse {
    private static Class<?> INITIAL_HANDLE_CLASS_STATE_CLASS;
    private static MethodHandle THIS_STATE;
    private static MethodHandle REQUEST;
    private static MethodHandle CHANNEL_WRAPPER;
    InitialHandler initialHandler;
    SecretKey sharedKey;
    EncryptionRequest request;

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
        initialHandler = (InitialHandler) handler;
        try {
            request = (EncryptionRequest) REQUEST.invoke(handler);
            addEncrypt();
            BungeeCord.getInstance().getScheduler().runAsync(MultiLoginBungee.INSTANCE, new AuthTask(initialHandler, genAuthMap()));
        } catch (Throwable e) {
            e.printStackTrace();
            initialHandler.disconnect(new TextComponent(PluginData.configurationConfig.getString("msgNoAdopt")));
            MultiCore.getPlugin().getPluginLogger().severe(I18n.getTransString("plugin_severe_io_user"));
        }
    }

    private void addEncrypt() throws Throwable {
        ChannelWrapper ch = (ChannelWrapper) CHANNEL_WRAPPER.invoke(initialHandler);
        Preconditions.checkState(THIS_STATE.invoke(initialHandler) == ReflectUtil.getEnumIns(INITIAL_HANDLE_CLASS_STATE_CLASS, "ENCRYPT"), "Not expecting ENCRYPT");
        sharedKey = EncryptionUtil.getSecret(this, request);
        if (sharedKey instanceof SecretKeySpec && sharedKey.getEncoded().length != 16) {
            ch.close();
            return;
        }
        BungeeCipher decrypt = EncryptionUtil.getCipher(false, sharedKey);
        ch.addBefore("frame-decoder", "decrypt", new CipherDecoder(decrypt));
        BungeeCipher encrypt = EncryptionUtil.getCipher(true, sharedKey);
        ch.addBefore("frame-prepender", "encrypt", new CipherEncoder(encrypt));
    }

    private String genServerId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        for (byte[] bit : new byte[][]{request.getServerId().getBytes("ISO_8859_1"), sharedKey.getEncoded(), EncryptionUtil.keys.getPublic().getEncoded()}) {
            sha.update(bit);
        }
        return (new BigInteger(sha.digest())).toString(16);
    }

    private Map<String, String> genAuthMap() throws UnsupportedEncodingException, NoSuchAlgorithmException {
        Map<String, String> map = new HashMap<>();
        map.put("username", initialHandler.getName());
        map.put("serverId", genServerId());
        if (BungeeCord.getInstance().config.isPreventProxyConnections() && initialHandler.getSocketAddress() instanceof InetSocketAddress) {
            map.put("ip", initialHandler.getAddress().getAddress().getHostAddress());
        }
        return map;
    }
}
