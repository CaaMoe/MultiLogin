package moe.caa.multilogin.api.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户配置
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Property {
    private String name;
    private String value;
    private String signature;

    @Override
    public Property clone() {
        return new Property(name, value, signature);
    }
}