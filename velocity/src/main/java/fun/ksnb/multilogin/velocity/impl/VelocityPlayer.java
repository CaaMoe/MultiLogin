package fun.ksnb.multilogin.velocity.impl;

import com.velocitypowered.api.proxy.Player;
import moe.caa.multilogin.api.plugin.IPlayer;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import net.kyori.adventure.text.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.Objects;
import java.util.UUID;

/**
 * Velocity 玩家对象
 */
public class VelocityPlayer extends VelocitySender implements IPlayer {
    private final Player player;

    private static MethodHandle setProfileField;
    private static MethodHandle setConnectedServerField;

    //    反射
    public static void init() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> father = Class.forName("com.velocitypowered.proxy.connection.client.ConnectedPlayer");
        Field profile = ReflectUtil.handleAccessible(
                father.getDeclaredField("profile")
        );
        setProfileField = lookup.unreflectSetter(profile);
        Field connectedServer = ReflectUtil.handleAccessible(
                father.getDeclaredField("connectedServer")
        );
        setConnectedServerField = lookup.unreflectSetter(connectedServer);

    }

    public VelocityPlayer(Player player) {
        super(player);
        this.player = player;

    }

    @Override
    public void kickPlayer(String message) {
        player.disconnect(Component.text(message));
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public SocketAddress getAddress() {
        return player.getRemoteAddress();
    }

    @Override
    public String getName() {
        return player.getUsername();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityPlayer that = (VelocityPlayer) o;
        return Objects.equals(player.getUniqueId(), that.player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(player.getUniqueId());
    }
}
