package `fun`.iiii.multilogin.velocity.core.util

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.proxy.connection.client.ConnectedPlayer
import `fun`.iiii.multilogin.velocity.core.inject.netty.MultiLoginChannelInitializer
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

fun Player.getMultiLoginData() {
    this as ConnectedPlayer
    connection.channel.pipeline()[MultiLoginChannelInitializer.MULTI_LOGIN_HANDLE_NAME]
}

fun Class<*>.getEnumConstant(name: String): Enum<*>? {
    return this.enumConstants.find {
        (it as Enum<*>).name == name
    } as Enum<*>?
}

fun MethodHandles.Lookup.unreflectMethodAccess(method: Method): MethodHandle =
    this.unreflect(method.apply { isAccessible = true })

fun MethodHandles.Lookup.unreflectFieldGetterAccess(field: Field): MethodHandle =
    this.unreflectGetter(field.apply { isAccessible = true })

fun MethodHandles.Lookup.unreflectFieldSetterAccess(field: Field): MethodHandle =
    this.unreflectSetter(field.apply { isAccessible = true })

fun <T> MethodHandles.Lookup.unreflectConstructorAccess(constructor: Constructor<T>): MethodHandle =
    this.unreflectConstructor(constructor.apply { isAccessible = true })