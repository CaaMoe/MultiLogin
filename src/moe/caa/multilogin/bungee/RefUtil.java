package moe.caa.multilogin.bungee;

import io.github.waterfallmc.waterfall.event.ConnectionInitEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.haproxy.HAProxyMessageDecoder;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.netty.HandlerBoss;
import net.md_5.bungee.netty.PipelineUtils;
import net.md_5.bungee.protocol.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.SocketAddress;
import java.util.Arrays;

public class RefUtil {

    public static Field getField(Class clazz, Class target){
        for(Field field : clazz.getDeclaredFields()){
            if(field.getType() == target){
                field.setAccessible(true);
                return field;
            }
        }
        throw new IllegalArgumentException();
    }

    public static Field getField(Class clazz, String target) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(target);
        field.setAccessible(true);
        return field;
    }

    public static Method getMethod(Class clazz, String name, Class... args){
        for(Method method : clazz.getDeclaredMethods()){
            if(method.getName().equalsIgnoreCase(name)){
                if(Arrays.equals(method.getParameterTypes(), args)){
                    method.setAccessible(true);
                    return method;
                }
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static Object getEnumIns(Class clazz,String name) {
        for (Object constant : clazz.getEnumConstants()) {
            if(constant.toString().equalsIgnoreCase(name)){
                return constant;
            }
        }
        throw new IllegalArgumentException(name);
    }


    public static void initService() throws IllegalAccessException, NoSuchFieldException {
        Class<PipelineUtils> pipelineUtilsClass = PipelineUtils.class;
        Field field = getField(pipelineUtilsClass, ChannelInitializer.class);
        Field field1 = getField(pipelineUtilsClass, KickStringWriter.class);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, new ChannelInitializer<Channel>() {
            protected void initChannel(Channel ch) {
                SocketAddress remoteAddress = ch.remoteAddress() == null ? ch.parent().localAddress() : ch.remoteAddress();
                if (BungeeCord.getInstance().getConnectionThrottle() != null && BungeeCord.getInstance().getConnectionThrottle().throttle(remoteAddress)) {
                    ch.close();
                } else {
                    ListenerInfo listener = ch.attr(PipelineUtils.LISTENER).get();
                    if (BungeeCord.getInstance().getPluginManager().callEvent(new ClientConnectEvent(remoteAddress, listener)).isCancelled()) {
                        ch.close();
                    } else {
                        ConnectionInitEvent connectionInitEvent = new ConnectionInitEvent(ch.remoteAddress(), listener, (result, throwable) -> {
                            if (result.isCancelled()) {
                                ch.close();
                            } else {
                                try {
                                    PipelineUtils.BASE.initChannel(ch);
                                } catch (Exception var5) {
                                    var5.printStackTrace();
                                    ch.close();
                                    return;
                                }

                                ch.pipeline().addBefore("frame-decoder", "legacy-decoder", new LegacyDecoder());
                                ch.pipeline().addAfter("frame-decoder", "packet-decoder", new MinecraftDecoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
                                ch.pipeline().addAfter("frame-prepender", "packet-encoder", new MinecraftEncoder(Protocol.HANDSHAKE, true, ProxyServer.getInstance().getProtocolVersion()));
                                try {
                                    ch.pipeline().addBefore("frame-prepender", "legacy-kick", (ChannelHandler) field1.get(null));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    ch.pipeline().get(HandlerBoss.class).setHandler(new MultiInitialHandler(BungeeCord.getInstance(), listener));
                                } catch (ClassNotFoundException | NoSuchFieldException e) {
                                    e.printStackTrace();
                                }
                                if (listener.isProxyProtocol()) {
                                    ch.pipeline().addFirst(new HAProxyMessageDecoder());
                                }
                            }
                        });
                        BungeeCord.getInstance().getPluginManager().callEvent(connectionInitEvent);
                    }
                }
            }
        });
    }
}
