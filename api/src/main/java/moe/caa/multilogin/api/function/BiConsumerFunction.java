package moe.caa.multilogin.api.function;

// 表示接受两个输入参数且不返回结果的操作
@FunctionalInterface
public interface BiConsumerFunction<T, U, R> {
    R accept(T t, U u);
}
