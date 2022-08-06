package fun.ksnb.multilogin.velocity.injector.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IdentifiedKeyInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println(method);
        return method.invoke(args);
    }
}
