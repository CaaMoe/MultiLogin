package moe.caa.multilogin.api.internal.util.reflect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 对象访问者
 */
@ApiStatus.Internal
@AllArgsConstructor
public class Accessor {
    @Getter
    private final Class<?> classHandle;

    private <V> List<V> getElements(V[] vs, Function<V, Boolean> function) {
        return Arrays.stream(vs).filter(function::apply).collect(Collectors.toList());
    }

    /**
     * 使用给定的函数检索所有 Method
     */
    public List<Method> findAllMethods(boolean declared, Function<Method, Boolean> function) {
        return getElements(declared ? classHandle.getDeclaredMethods() : classHandle.getMethods(), function);
    }

    /**
     * 使用给定的函数检索所有 Field
     */
    public List<Field> findAllFields(boolean declared, Function<Field, Boolean> function) {
        return getElements(declared ? classHandle.getDeclaredFields() : classHandle.getFields(), function);
    }

    /**
     * 使用给定的函数检索所有 Constructor
     */
    public List<Constructor<?>> findAllConstructors(boolean declared, Function<Constructor<?>, Boolean> function) {
        return getElements(declared ? classHandle.getDeclaredConstructors() : classHandle.getConstructors(), function);
    }

    /**
     * 使用给定的函数检索第一个出现的 Method
     */
    public Method findFirstMethod(boolean declared, Function<Method, Boolean> function, String exceptionMessage) throws NoSuchMethodException {
        List<Method> elements = getElements(declared ? classHandle.getDeclaredMethods() : classHandle.getMethods(), function);
        if (elements.size() == 0) throw new NoSuchMethodException(exceptionMessage);
        return elements.get(0);
    }

    /**
     * 使用给定的函数检索第一个出现的 Field
     */
    public Field findFirstField(boolean declared, Function<Field, Boolean> function, String exceptionMessage) throws NoSuchFieldException {
        List<Field> elements = getElements(declared ? classHandle.getDeclaredFields() : classHandle.getFields(), function);
        if (elements.size() == 0) throw new NoSuchFieldException(exceptionMessage);
        return elements.get(0);
    }

    /**
     * 使用给定的函数检索第一个出现的 Constructor
     */
    public Constructor<?> findFirstConstructors(boolean declared, Function<Constructor<?>, Boolean> function, String exceptionMessage) throws NoSuchConstructorException {
        List<Constructor<?>> elements = getElements(declared ? classHandle.getDeclaredConstructors() : classHandle.getConstructors(), function);
        if (elements.size() == 0) throw new NoSuchConstructorException(exceptionMessage);
        return elements.get(0);
    }

    /**
     * 使用给定的名称检索第一次出现的 Method
     */
    public Method findFirstMethodByName(boolean declared, String name) throws NoSuchMethodException {
        return findFirstMethod(declared, m -> m.getName().equals(name), String.format("%s(dedicated = %b) -> %s", classHandle.getName(), declared, name));
    }

    /**
     * 使用给定的入参类型检索第一次出现的 Method
     */
    public Method findFirstMethodByParameterTypes(boolean declared, Type[] types) throws NoSuchMethodException {
        return findFirstMethod(declared, m -> Arrays.equals(types, m.getParameterTypes()), String.format("%s(dedicated = %b) -> %s", classHandle.getName(), declared, Arrays.toString(types)));
    }

    /**
     * 使用给定的返回值类型检索第一次出现的 Method
     */
    public Method findFirstMethodByReturnType(boolean declared, Type returnType) throws NoSuchMethodException {
        return findFirstMethod(declared, m -> m.getReturnType().equals(returnType), String.format("%s(dedicated = %b) -> returnType = %s", classHandle.getName(), declared, returnType));
    }

    /**
     * 使用给定的名称检索第一次出现的 Field
     */
    public Field findFirstFieldByName(boolean declared, String name) throws NoSuchFieldException {
        return findFirstField(declared, f -> f.getName().equals(name), String.format("%s(dedicated = %b) -> %s", classHandle.getName(), declared, name));
    }

    /**
     * 使用给定的类型检索第一次出现的 Field
     */
    public Field findFirstFieldByType(boolean declared, Type fieldType) throws NoSuchFieldException {
        return findFirstField(declared, f -> f.getType().equals(fieldType), String.format("%s(dedicated = %b) -> %s", classHandle.getName(), declared, fieldType));
    }

    /**
     * 使用给定的类型检索第一次出现的 Field
     */
    public Constructor<?> findFirstConstructorByParameterTypes(boolean declared, Type[] types) throws NoSuchConstructorException {
        return findFirstConstructors(declared, c -> Arrays.equals(c.getParameterTypes(), types), String.format("%s(dedicated = %b) -> %s", classHandle.getName(), declared, types));
    }
}
