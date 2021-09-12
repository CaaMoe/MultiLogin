package moe.caa.multilogin.core.auth.response;

import lombok.Data;

/**
 * 从 Yggdrasil 账户验证服务器获取到的用户配置
 */
@Data
public class Property{
    private String name;
    private String value;
    private String signature;
}