package moe.caa.multilogin.core.auth.yggdrasil;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 代表用户验证数据
 */
@ToString
@AllArgsConstructor
@EqualsAndHashCode
public class YggdrasilUserData {
    protected final String username;
    protected final String serverId;
    protected final String ip;
}
