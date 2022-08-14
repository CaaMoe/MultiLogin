package moe.caa.multilogin.api.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表示一堆对象
 */
@Data
@AllArgsConstructor
public class There<V1, V2, V3> {
    private final V1 value1;
    private final V2 value2;
    private final V3 value3;
}
