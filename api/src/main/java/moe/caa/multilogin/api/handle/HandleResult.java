package moe.caa.multilogin.api.handle;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表示一个通讯结果
 */
@Getter
@AllArgsConstructor
public class HandleResult {
    // 通讯结果类型
    private final Type type;
    // 如果踢出，显示踢出消息
    private final String kickMessage;

    public enum Type {
        NONE,
        KICK;
    }
}
