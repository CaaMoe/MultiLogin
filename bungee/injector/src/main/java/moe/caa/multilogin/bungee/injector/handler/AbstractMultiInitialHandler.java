package moe.caa.multilogin.bungee.injector.handler;

import lombok.Getter;
import moe.caa.multilogin.api.main.MultiCoreAPI;
import moe.caa.multilogin.api.util.ReflectUtil;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Objects;

/**
 * 接管 net.md_5.bungee.connection.InitialHandler 类的其中一个方法
 */
@Getter
public abstract class AbstractMultiInitialHandler {
    // LoginStateEnum 的枚举
    protected static Enum<?> state$HANDSHAKE;
    protected static Enum<?> state$STATUS;
    protected static Enum<?> state$PING;
    protected static Enum<?> state$USERNAME;
    protected static Enum<?> state$ENCRYPT;
    protected static Enum<?> state$FINISHING;

    /*
     * Getter
     */
    protected static MethodHandle thisStateFieldGetter;
    protected static MethodHandle loginRequestFieldGetter;
    protected static MethodHandle requestFieldGetter;
    protected static MethodHandle chFieldGetter;

    /*
     * Method
     */
    protected static MethodHandle getNameMethod;
    protected static MethodHandle getSocketAddressMethod;
    protected static MethodHandle getAddressMethod;
    protected static MethodHandle finishMethod;

    /*
     * Setter
     */
    protected static MethodHandle loginProfileFieldSetter;
    protected static MethodHandle nameFieldSetter;
    protected static MethodHandle uniqueIdFieldSetter;

    protected final MultiCoreAPI multiCoreAPI;
    protected final InitialHandler initialHandler;

    public AbstractMultiInitialHandler(InitialHandler initialHandler, MultiCoreAPI multiCoreAPI) {
        this.initialHandler = initialHandler;
        this.multiCoreAPI = multiCoreAPI;
    }

    public static void init() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, NoSuchFieldException {
        Class<?> stateEnum = Class.forName("net.md_5.bungee.connection.InitialHandler$State");

        // 获取枚举常量
        for (Object constant : stateEnum.getEnumConstants()) {
            final Enum<?> enumObject = (Enum<?>) constant;
            switch ((enumObject).name()) {
                case "HANDSHAKE":
                    state$HANDSHAKE = enumObject;
                    break;
                case "STATUS":
                    state$STATUS = enumObject;
                    break;
                case "PING":
                    state$PING = enumObject;
                    break;
                case "USERNAME":
                    state$USERNAME = enumObject;
                    break;
                case "ENCRYPT":
                    state$ENCRYPT = enumObject;
                    break;
                case "FINISHING":
                    state$FINISHING = enumObject;
                    break;
            }
        }

        Objects.requireNonNull(state$HANDSHAKE, "HANDSHAKE");
        Objects.requireNonNull(state$STATUS, "STATUS");
        Objects.requireNonNull(state$PING, "PING");
        Objects.requireNonNull(state$USERNAME, "USERNAME");
        Objects.requireNonNull(state$ENCRYPT, "ENCRYPT");
        Objects.requireNonNull(state$FINISHING, "FINISHING");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        thisStateFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("thisState")
        ));

        loginRequestFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("loginRequest")
        ));

        requestFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("request")
        ));

        chFieldGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("ch")
        ));

        getNameMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredMethod("getName")
        ));

        getSocketAddressMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredMethod("getSocketAddress")
        ));
        getAddressMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredMethod("getAddress")
        ));

        finishMethod = lookup.unreflect(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredMethod("finish")
        ));

        loginProfileFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("loginProfile")
        ));

        nameFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("name")
        ));

        uniqueIdFieldSetter = lookup.unreflectSetter(ReflectUtil.handleAccessible(
                InitialHandler.class.getDeclaredField("uniqueId")
        ));
    }
}
