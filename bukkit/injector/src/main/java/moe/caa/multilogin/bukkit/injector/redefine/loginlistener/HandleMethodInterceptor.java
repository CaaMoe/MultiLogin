package moe.caa.multilogin.bukkit.injector.redefine.loginlistener;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HandleMethodInterceptor {

    // 在这，不能直接引用插件的类
    @Advice.OnMethodEnter
    public static void intercept(
            @Advice.This Object thisObj,
            @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args
    ) throws Throwable {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MultiLogin");
        Field loader = plugin.getClass().getDeclaredField("mlClassLoader");
        loader.setAccessible(true);
        ClassLoader classLoader = (ClassLoader) loader.get(plugin);
        Method handle = classLoader.loadClass(
                "moe.caa.multilogin.bukkit.injector.redefine.loginlistener.DockHandler"
        ).getMethod("handle", Object.class, Object[].class);
        MethodHandle mockHandle = MethodHandles.lookup().unreflect(handle);
        args = (Object[]) mockHandle.invoke(thisObj, args);
    }
}