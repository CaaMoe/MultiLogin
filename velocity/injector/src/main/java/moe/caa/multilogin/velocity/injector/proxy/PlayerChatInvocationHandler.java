package moe.caa.multilogin.velocity.injector.proxy;

import com.velocitypowered.proxy.protocol.packet.chat.PlayerChat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class PlayerChatInvocationHandler implements InvocationHandler {
    private static MethodHandle signatureFieldSetter;
    private static MethodHandle saltFieldSetter;
    private final Object obj;

    public PlayerChatInvocationHandler(Object obj) {
        this.obj = obj;
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        Field signatureField = PlayerChat.class.getDeclaredField("signature");
        signatureField.setAccessible(true);
        PlayerChatInvocationHandler.signatureFieldSetter = lookup.unreflectSetter(signatureField);

        Field saltField = PlayerChat.class.getDeclaredField("salt");
        saltField.setAccessible(true);
        PlayerChatInvocationHandler.saltFieldSetter = lookup.unreflectSetter(saltField);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(obj, args);
    }
}
