package moe.caa.multilogin.core.auth.response;

import lombok.Data;

import java.util.Map;
import java.util.UUID;

/**
 * 登入 HasJoined 阶段从 Yggdrasil 账户验证服务器获取到的有效的结果
 */
@Data
public class HasJoinedResponse {
    private UUID id;
    private String name;
    private Map<String, Property> propertyMap;

    public boolean isSucceed() {
        return id != null;
    }
}
