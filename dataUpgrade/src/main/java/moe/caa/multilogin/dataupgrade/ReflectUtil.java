package moe.caa.multilogin.dataupgrade;

import sun.misc.Unsafe;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.URL;

public class ReflectUtil {

    /**
     * 获得超级反射权限
     */
    public static MethodHandles.Lookup getSuperLookup() throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);

        Field impl_lookup = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        return (MethodHandles.Lookup) unsafe.getObject(MethodHandles.Lookup.class, unsafe.staticFieldOffset(impl_lookup));
    }
}
