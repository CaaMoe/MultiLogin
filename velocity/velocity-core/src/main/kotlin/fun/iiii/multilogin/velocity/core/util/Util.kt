package `fun`.iiii.multilogin.velocity.core.util

import com.velocitypowered.api.util.GameProfile
import com.velocitypowered.api.util.GameProfile.Property
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

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

fun moe.caa.multilogin.api.profile.GameProfile.toVelocityGameProfile(): GameProfile {
    return GameProfile(this.uuid, this.username, this.properties.map { it.toVelocityProperty() })
}

fun GameProfile.toMultiLoginGameProfile(): moe.caa.multilogin.api.profile.GameProfile {
    return moe.caa.multilogin.api.profile.GameProfile(
        this.id,
        this.name,
        this.properties.map { it.toMultiLoginProperty() })
}

fun moe.caa.multilogin.api.profile.GameProfile.Property.toVelocityProperty(): Property {
    return Property(this.name, this.value, this.signature)
}

fun Property.toMultiLoginProperty(): moe.caa.multilogin.api.profile.GameProfile.Property {
    return moe.caa.multilogin.api.profile.GameProfile.Property(this.name, this.value, this.signature)
}