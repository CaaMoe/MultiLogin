package moe.caa.multilogin.bukkit.main;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.SneakyThrows;
import moe.caa.multilogin.bukkit.nms.v1_16_R3.proxy.MultiPacketLoginInEncryptionBegin;
import moe.caa.multilogin.core.impl.IPlugin;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.util.ReflectUtil;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MultiLoginBukkit extends JavaPlugin implements IPlugin {

    @SneakyThrows
    @Override
    @SuppressWarnings("all")
    public void onEnable() {
        Class<?> clazz = Class.forName("net.minecraft.server.v1_16_R3.EnumProtocol");
        Field jField = ReflectUtil.handleAccessible(clazz.getDeclaredField("h"), true);
        Map<EnumProtocolDirection, ?> jValue = (Map<EnumProtocolDirection, ?>) jField.get(EnumProtocol.LOGIN);

        Class<?> aClass = Class.forName("net.minecraft.server.v1_16_R3.EnumProtocol$a");

        Object aValue = jValue.get(EnumProtocolDirection.SERVERBOUND);
        Object2IntMap aV = (Object2IntMap) ReflectUtil.handleAccessible(aClass.getDeclaredField("a"), true).get(aValue);
        List<Supplier<? extends Packet<?>>> bV = (List) ReflectUtil.handleAccessible(aClass.getDeclaredField("b"), true).get(aValue);

        aV.clear();
        bV.clear();

        aV.put(PacketLoginInStart.class, 0);
        bV.add(PacketLoginInStart::new);

        aV.put(PacketLoginInEncryptionBegin.class, 1);
        bV.add(MultiPacketLoginInEncryptionBegin::new);

        aV.put(PacketLoginInCustomPayload.class, 2);
        bV.add(PacketLoginInCustomPayload::new);
    }

    @Override
    public void initService() throws ClassNotFoundException {
        // net.minecraft.network.EnumProtocol

    }

    @Override
    public void initOther() {

    }

    @Override
    public void loggerLog(LoggerLevel level, String message, Throwable throwable) {

    }
}
