package moe.caa.multilogin.api.internal.util.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

/**
 * 美剧变量访问
 */
@ApiStatus.Internal
@AllArgsConstructor
public class EnumAccessor {
    @Getter
    private final Class<?> enumClass;

    /**
     * 返回所有枚举常量
     */
    public Enum<?>[] getValues() {
        return (Enum<?>[]) enumClass.getEnumConstants();
    }

    /**
     * 返回指定索引枚举常量
     */
    public Enum<?> indexOf(int index) {
        return getValues()[index];
    }

    public Enum<?> findByName(String name) throws NoSuchEnumException {
        for (Enum<?> value : getValues()) {
            if (value.name().equals(name)) {
                return value;
            }
        }
        throw new NoSuchEnumException(String.format("%s -> %s", enumClass.getName(), name));
    }
}
