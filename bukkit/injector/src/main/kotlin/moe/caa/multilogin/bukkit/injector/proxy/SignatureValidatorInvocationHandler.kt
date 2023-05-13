package moe.caa.multilogin.bukkit.injector.proxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class SignatureValidatorInvocationHandler(
    private val obj: Any
) : InvocationHandler {

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any {
        if (method.returnType == Boolean.Companion::class.java) {
            return true
        }
        return method.invoke(obj, *args)
    }
}