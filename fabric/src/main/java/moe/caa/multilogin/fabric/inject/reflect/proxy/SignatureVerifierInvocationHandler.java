package moe.caa.multilogin.fabric.inject.reflect.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * JDK 代理 SignatureValidator
 */
public class SignatureVerifierInvocationHandler implements InvocationHandler {
    private final Object obj;

    public SignatureVerifierInvocationHandler(Object obj) {
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