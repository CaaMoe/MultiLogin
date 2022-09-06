package moe.caa.multilogin.bukkit.injector.proxy;

import com.mojang.authlib.GameProfile;
import moe.caa.multilogin.api.util.reflect.ReflectUtil;
import moe.caa.multilogin.bukkit.injector.BukkitInjector;
import moe.caa.multilogin.bukkit.injector.Contents;
import moe.caa.multilogin.bukkit.main.MultiLoginBukkit;
import moe.caa.multilogin.core.proxy.InterceptMethodInvocationHandler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.InvocationHandlerAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;

public class PacketLoginInEncryptionBeginInvocationHandler extends InterceptMethodInvocationHandler {
    private static Class<?> proxyClass;
    private static MethodHandle multilgoin_original_handlerFieldSetter;
    private static MethodHandle multilgoin_original_handlerFieldGetter;
    private static MethodHandle gameProfileGetter;
    private static MethodHandle chatDeserializer;

    /**
     * @param handle 被代理的类
     */
    public PacketLoginInEncryptionBeginInvocationHandler(Object handle) {
        super(handle, m -> m.getParameterTypes().length == 1 && m.getParameterTypes()[0].isAssignableFrom(BukkitInjector.getPacketListenerClass()), ((method, objects) -> {
            try {
                Object proxyObj = newFakeProxyLoginListener(objects[0], proxyClass);
                apply(BukkitInjector.getLoginListenerClass(), objects[0], proxyObj);

                objects[0] = proxyObj;
                return method.invoke(handle, objects);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }));
    }

    public static void init() throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        String name = "moe.caa.multilogin.bukkit.injector.LoginListenerProxy$" + UUID.randomUUID().toString().substring(0, 6);
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        gameProfileGetter = lookup.unreflectGetter(ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(BukkitInjector.getLoginListenerClass(), GameProfile.class)));
        String original_handler = "multilgoin_original_handler";
        proxyClass = new ByteBuddy()
                .subclass(BukkitInjector.getLoginListenerClass())
                .name(name)
                .defineField(original_handler, BukkitInjector.getLoginListenerClass(), Visibility.PUBLIC)
                // 假代理全部方法，代理值更改需要实时同步到原来的值上!
                // todo
                // 这里有个难以实现的地方
                // 方法全部代理，但是执行只能调用原对象，这样假代理将会失效

                // 如果不代理 public void a(PacketLoginInEncryptionBegin packetlogininencryptionbegin) 这个方法，
                //           就不能用它调用原来值的方法，就不知道这个方法什么时候结束，就不能正常将代理的值应用到原来的值上而导致登录时间过长（监听AsyncLogin又摸不到这个代理类）
                //
                // 如果不代理这个方法，就不能实现预期效果（重写踢出方法）

                // 这个代理将要实现的是： 修改玩家的踢出消息（代理非接口方法）。
                .method(m -> true).intercept(InvocationHandlerAdapter.of((proxy, method, args) -> {
                    Object origin = multilgoin_original_handlerFieldGetter.invoke(proxy);
                    if (args.length == 1) {
                        System.out.println(args[0]);
                        GameProfile profile = (GameProfile) gameProfileGetter.invoke(proxy);
                        if (args[0].getClass().equals(String.class)) {
                            Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
                            if (remove != null) {
                                System.out.println(remove.getKickMessage());
                                args[0] = remove.getKickMessage();
                            }
                        } else if (args[0].getClass().equals(BukkitInjector.getIChatBaseComponentClass())) {
                            Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
                            if (remove != null) {
                                System.out.println(remove.getKickMessage());
                                args[0] = chatDeserializer.invoke(remove.getKickMessage());
                            }
                        }
                    }
                    Object invoke = method.invoke(origin, args);
                    // 将代理类的所有属性赋给原始操作对象
                    apply(BukkitInjector.getLoginListenerClass(), proxy, origin);
                    return invoke;
                }))
                .make()
                .load(((MultiLoginBukkit) BukkitInjector.getApi().getPlugin()).getMlPluginLoader()
                        .getPluginClassLoader().self())
                .getLoaded();

        Field field = proxyClass.getField(original_handler);

        multilgoin_original_handlerFieldSetter = lookup.unreflectSetter(field);
        multilgoin_original_handlerFieldGetter = lookup.unreflectGetter(field);

        chatDeserializer = lookup.unreflect(ReflectUtil.handleAccessible(ReflectUtil.findStaticMethodByReturnTypeAndParameters(
                BukkitInjector.getIChatBaseComponent$chatSerializerClass(), BukkitInjector.getIChatMutableComponentClass(), String.class)));
    }

    private static Object newFakeProxyLoginListener(Object source, Class<?> byteBuddyProxyClass) throws Throwable {
        Constructor<?> constructor = byteBuddyProxyClass.getDeclaredConstructors()[0];
        Object[] parameters = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterTypes().length; i++) {
            parameters[i] = ReflectUtil.handleAccessible(ReflectUtil.findNoStaticField(BukkitInjector.getLoginListenerClass(), constructor.getParameterTypes()[i])).get(source);
        }
        Object o = constructor.newInstance(parameters);
        // 绑定 原来的 handler
        multilgoin_original_handlerFieldSetter.invoke(o, source);
        return o;
    }

    public static <T> void apply(Class<?> mb, T source, T target) throws IllegalAccessException {
        for (Field declaredField : mb.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
        for (Field declaredField : mb.getFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())) continue;
            Field field = ReflectUtil.handleAccessible(declaredField);
            field.set(target, field.get(source));
        }
    }
}
