package moe.caa.multilogin.common.internal.util;

import java.lang.reflect.AccessibleObject;

public class ReflectUtil {
    public static <T extends AccessibleObject> T openAccess(T accessibleObject) {
        accessibleObject.setAccessible(true);
        return accessibleObject;
    }
}
