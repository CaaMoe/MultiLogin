package moe.caa.multilogin.velocity.injector.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class MinecraftSessionHandlerInvocationHandler implements InvocationHandler {
    private final Object obj;

    public MinecraftSessionHandlerInvocationHandler(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(obj, args);
    }
}
