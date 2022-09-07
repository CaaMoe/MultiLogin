package moe.caa.multilogin.api.function;

// 可抛出的函数
@FunctionalInterface
public interface ThrowFunction<T, R> {
    R apply(T t) throws Throwable;
}
