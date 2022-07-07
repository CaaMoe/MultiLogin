package moe.caa.multilogin.api.auth;

import lombok.Data;

/**
 * 从 Yggdrasil 账户验证服务器获取到的用户配置
 */
@Data
public class Property {
    private String name;
    private String value;
    private String signature;

    @Override
    public Property clone() {
        Property property = new Property();
        property.name = name;
        property.value = value;
        property.signature = signature;
        return property;
    }
}