package moe.caa.multilogin.api.internal.function;

import org.jetbrains.annotations.ApiStatus;

// 可抛出的函数
@FunctionalInterface
@ApiStatus.Internal
public interface ThrowFunction<T, R> {
    R apply(T t) throws Throwable;
}
