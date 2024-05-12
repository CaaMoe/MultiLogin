package moe.caa.multilogin.api.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(name, property.name) && Objects.equals(value, property.value) && Objects.equals(signature, property.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, signature);
    }
}