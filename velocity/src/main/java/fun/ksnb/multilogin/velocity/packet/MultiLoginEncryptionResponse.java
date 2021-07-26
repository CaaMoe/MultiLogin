/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * fun.ksnb.multilogin.velocity.proxy.EncryptionResponse
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package fun.ksnb.multilogin.velocity.packet;

import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.MinecraftSessionHandler;
import com.velocitypowered.proxy.connection.client.InitialInboundConnection;
import com.velocitypowered.proxy.connection.client.LoginSessionHandler;
import com.velocitypowered.proxy.protocol.packet.EncryptionResponse;
import com.velocitypowered.proxy.protocol.packet.ServerLogin;
import fun.ksnb.multilogin.velocity.main.MultiLoginVelocity;
import fun.ksnb.multilogin.velocity.task.VelocityAuthTask;
import moe.caa.multilogin.core.main.MultiCore;
import moe.caa.multilogin.core.util.ReflectUtil;

import java.lang.invoke.MethodHandle;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;

import static com.velocitypowered.proxy.connection.VelocityConstants.EMPTY_BYTE_ARRAY;
import static com.velocitypowered.proxy.util.EncryptionUtils.decryptRsa;
import static com.velocitypowered.proxy.util.EncryptionUtils.generateServerId;

public class MultiLoginEncryptionResponse extends EncryptionResponse {
    private static MethodHandle SERVERLOGIN;
    private static MethodHandle VERIFY;
    private static MethodHandle MCCONNECTION;
    private static MethodHandle INBOUND;

    private final VelocityServer server;
    //    原有
    ServerLogin login = null;
    byte[] verify = EMPTY_BYTE_ARRAY;
    LoginSessionHandler loginSessionHandler;
    //    运行种产生
    byte[] decryptedSharedSecret = EMPTY_BYTE_ARRAY;
    KeyPair serverKeyPair;
    private MultiCore core;
    private MinecraftConnection mcConnection;
    private InitialInboundConnection inbound;

    public MultiLoginEncryptionResponse() {
        server = (VelocityServer) MultiLoginVelocity.getServer();
        core = MultiLoginVelocity.getInstance().getMultiCore();
    }

    public static void init() throws IllegalAccessException, NoSuchFieldException {
        SERVERLOGIN = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(LoginSessionHandler.class, "login", false));
        VERIFY = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(LoginSessionHandler.class, "verify", false));
        MCCONNECTION = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(LoginSessionHandler.class, "mcConnection", false));
        INBOUND = ReflectUtil.super_lookup.unreflectGetter(ReflectUtil.getField(LoginSessionHandler.class, "inbound", false));
    }

    @Override
    public boolean handle(MinecraftSessionHandler handler) {
        if (!(handler instanceof LoginSessionHandler)) {
//            不合法
            return false;
        }
        loginSessionHandler = (LoginSessionHandler) handler;
        getValues();
//        模拟常规流程
        if (login == null) {
            throw new IllegalStateException("尚未收到服务器登录数据包。");
        }
        if (verify.length == 0) {
            throw new IllegalStateException("尚未发送EncryptionRequest数据包。");
        }
        if (!enableEncrypt()) {
//            无法启用加密的情况
            mcConnection.close(true);
            return true;
        }
        String username = login.getUsername();
        String serverId = generateServerId(decryptedSharedSecret, serverKeyPair.getPublic());
        String playerIp = ((InetSocketAddress) mcConnection.getRemoteAddress()).getHostString();

//            交给任务处理
        MultiLoginVelocity.getInstance().getSchedule().runTaskAsync(new VelocityAuthTask(loginSessionHandler, username, serverId, playerIp, core, inbound));

        return true;
    }

    private boolean enableEncrypt() {
        try {
            serverKeyPair = server.getServerKeyPair();
            byte[] decryptedVerifyToken = decryptRsa(serverKeyPair, getVerifyToken());
            if (!MessageDigest.isEqual(verify, decryptedVerifyToken)) {
                throw new IllegalStateException("无法成功解密验证令牌。");
            }
            if (mcConnection.isClosed()) {
                // 玩家关了.
                return false;
            }

            decryptedSharedSecret = decryptRsa(serverKeyPair, getSharedSecret());
            try {
                mcConnection.enableEncryption(decryptedSharedSecret);
            } catch (GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        } catch (GeneralSecurityException e) {
//            无法启用加密
            return false;
        }
        return true;
    }

    //    对数值进行初始化
    private void getValues() {
        try {
            login = (ServerLogin) SERVERLOGIN.invoke(loginSessionHandler);
            verify = (byte[]) VERIFY.invoke(loginSessionHandler);
            mcConnection = (MinecraftConnection) MCCONNECTION.invoke(loginSessionHandler);
            inbound = (InitialInboundConnection) INBOUND.invoke(loginSessionHandler);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
    }
}
