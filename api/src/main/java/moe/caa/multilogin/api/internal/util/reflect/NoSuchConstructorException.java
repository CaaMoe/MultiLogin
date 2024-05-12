package moe.caa.multilogin.api.internal.util.reflect;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NoSuchConstructorException extends ReflectiveOperationException {

    /**
     * Constructor with a detail message.
     *
     * @param s the detail message
     */
    public NoSuchConstructorException(String s) {
        super(s);
    }
}
