package moe.caa.multilogin.api.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 表示一对对象
 */
@Data
@AllArgsConstructor
public class Pair<V1, V2> {
    private final V1 value1;
    private final V2 value2;
}
