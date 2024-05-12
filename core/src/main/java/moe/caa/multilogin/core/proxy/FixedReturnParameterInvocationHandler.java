package moe.caa.multilogin.core.proxy;

import moe.caa.multilogin.api.internal.function.BiConsumerFunction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

/**
 * 固定返回参数代理
 */
public class FixedReturnParameterInvocationHandler implements InvocationHandler {
    private final Object handle;
    private final Function<Method, Boolean> match;
    private final BiConsumerFunction<Object, Object, Object> fixedFunc;

    /**
     * @param handle    被代理的类
     * @param match     过滤规则
     * @param fixedFunc 返回值修改函数
     */
    public FixedReturnParameterInvocationHandler(Object handle, Function<Method, Boolean> match, BiConsumerFunction<Object, Object, Object> fixedFunc) {
        this.handle = handle;
        this.match = match;
        this.fixedFunc = fixedFunc;

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
    public Object invoke(Object proxy, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (match.apply(method)) {
            return fixedFunc.accept(handle, args);
        }
        return method.invoke(handle, args);
    }
}
