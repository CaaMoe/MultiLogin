package moe.caa.multilogin.bukkit.injector.proxy;

public class LoginListenerInvocationHandler {


    public static class ProxyMethodInterceptor {
//        @RuntimeType
//        public static Object intercept(
//                @SuperCall Callable<Object> supercall,
//                @This Object proxyObj,
//                @Origin Method method,
//                @AllArguments Object[] args) throws Throwable {
//
//            Object origin = multilgoin_original_handlerFieldGetter.invoke(proxyObj);
//            // 调用前同步数据
//            apply(BukkitInjector.getLoginListenerClass(), origin, proxyObj);
//
//            Object arg = args[0];
//            Class<?> argType = arg.getClass();
//            if (!argType.equals(BukkitInjector.getPacketLoginInEncryptionBeginClass())) {
//                if (argType.equals(String.class) || BukkitInjector.getIChatBaseComponentClass().isAssignableFrom(argType)) {
//                    GameProfile profile = (GameProfile) gameProfileGetter.invoke(proxyObj);
//                    Contents.KickMessageEntry remove = Contents.getKickMessageEntryMap().remove(profile.getName());
//                    if (remove != null) {
//                        if (arg.getClass().equals(String.class)) {
//                            arg = remove.getKickMessage();
//                        } else {
//                            arg = chatComponentConstructor.invoke(remove.getKickMessage());
//                        }
//                    }
//                    args[0] = arg;
//                    return method.invoke(origin, args);
//                }
//            }
//            Object invoke = supercall.call();
//            // 执行后跟进数据
//            apply(BukkitInjector.getLoginListenerClass(), proxyObj, origin);
//
//            if (argType.equals(BukkitInjector.getPacketLoginInEncryptionBeginClass())) {
//                LoginListenerSynchronizer.getInstance().putEntry(proxyObj, origin);
//            }
//            return invoke;
//        }
    }
}
