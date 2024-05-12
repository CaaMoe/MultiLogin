package moe.caa.multilogin.api.internal.function;

import org.jetbrains.annotations.ApiStatus;

// 表示接受两个输入参数且不返回结果的操作
@FunctionalInterface
@ApiStatus.Internal
public interface BiConsumerFunction<T, U, R> {
    R accept(T t, U u);
}
