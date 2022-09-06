package moe.caa.multilogin.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 方法拦截器
 */
public class InterceptMethodInvocationHandler implements InvocationHandler {
    private final Object handle;
    private final Function<Method, Boolean> match;
    private final BiFunction<Method, Object[], Object> redirect;

    /**
     * @param handle   被代理的类
     * @param match    过滤规则
     * @param redirect 方法重定向
     */
    public InterceptMethodInvocationHandler(Object handle, Function<Method, Boolean> match, BiFunction<Method, Object[], Object> redirect) {
        this.handle = handle;
        this.match = match;
        this.redirect = redirect;

        for (Method method : handle.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (match.apply(method)) {
                return;
            }
        }
        for (Method method : handle.getClass().getMethods()) {
            if (Modifier.isStatic(method.getModifiers())) continue;
            if (match.apply(method)) {
                return;
            }
        }

        throw new RuntimeException("Methods may never be matched.");
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (match.apply(method)) {
            return redirect.apply(method, args);
        }
        return method.invoke(handle, args);
    }
}
