package moe.caa.multilogin.bukkit.injector.nms.v1_19_r1.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class SignatureValidatorInvocationHandler implements InvocationHandler {
    private final Object obj;

    public SignatureValidatorInvocationHandler(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getReturnType() == boolean.class) {
            return true;
        }
        return method.invoke(obj, args);
    }
}