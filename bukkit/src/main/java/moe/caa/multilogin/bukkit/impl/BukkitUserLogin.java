package moe.caa.multilogin.bukkit.impl;

import lombok.SneakyThrows;
import moe.caa.multilogin.core.impl.IUserLogin;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.minecraft.server.v1_16_R3.CryptographyException;
import net.minecraft.server.v1_16_R3.LoginListener;
import net.minecraft.server.v1_16_R3.MinecraftEncryption;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.security.Key;
import java.security.PrivateKey;

public class BukkitUserLogin implements IUserLogin {
    private final LoginListener vanHandler;

    public BukkitUserLogin(LoginListener vanHandler) {
        this.vanHandler = vanHandler;
    }

    @Override
    public void disconnect(String message) {
        vanHandler.disconnect(message);
    }

    @Override
    public String getUsername() {
        return vanHandler.getGameProfile().getName();
    }

    @Override
    public String getServerId() {
        return null;
    }

    @Override
    public String getIp() {
        return null;
    }

    @SneakyThrows
    @Override
    public String startEncrypting() {
        Field g = ReflectUtil.handleAccessible(vanHandler.getClass().getDeclaredField("g"), true);
        Validate.validState(
                g.get(vanHandler) == ReflectUtil.getEnumIns((Class<? extends Enum<?>>)
                        Class.forName("net.minecraft.server.v1_16_R3.LoginListener$EnumProtocolState"), "KEY")
                , "Unexpected key packet");
        MinecraftServer mc = (MinecraftServer) ReflectUtil.handleAccessible(vanHandler.getClass().getDeclaredField("server"), true).get(vanHandler);
        PrivateKey privatekey = mc.getKeyPair().getPrivate();

        final String s;
        try {
            byte[] e = (byte[]) ReflectUtil.handleAccessible(vanHandler.getClass().getDeclaredField("e"), true).get(vanHandler);
            ReflectUtil.handleAccessible(vanHandler.getClass().getDeclaredMethod("pac"), true);

            // TODO: 2021/10/2
//            if (!Arrays.equals(e, packetlogininencryptionbegin.b(privatekey))) {
//                throw new IllegalStateException("Protocol error");
//            }
//

            Field loginKey = ReflectUtil.handleAccessible(vanHandler.getClass().getDeclaredField("loginKey"), true);

            // TODO: 2021/10/2
            // this.loginKey = packetlogininencryptionbegin.a(privatekey);
            Cipher cipher = MinecraftEncryption.a(2, (Key) loginKey.get(vanHandler));
            Cipher cipher1 = MinecraftEncryption.a(1, (Key) loginKey.get(vanHandler));
            s = (new BigInteger(MinecraftEncryption.a("", mc.getKeyPair().getPublic(), (SecretKey) loginKey.get(vanHandler)))).toString(16);


            g.set(vanHandler, ReflectUtil.getEnumIns(
                    (Class<? extends Enum<?>>) Class.forName("net.minecraft.server.v1_16_R3.LoginListener$EnumProtocolState")
                    , "EnumProtocolState"));

            // TODO: 2021/10/2
            // this.networkManager.a(cipher, cipher1);
        } catch (CryptographyException var6) {
            throw new IllegalStateException("Protocol error", var6);
        }

        return s;
    }

    @Override
    public void finish() {

    }
}
