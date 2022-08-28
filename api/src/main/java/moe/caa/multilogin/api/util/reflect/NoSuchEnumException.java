package moe.caa.multilogin.api.util.reflect;

/**
 * Signals that the enum class doesn't have an enum of a specified name.
 */
public class NoSuchEnumException extends ReflectiveOperationException {

    /**
     * Constructor with a detail message.
     *
     * @param s the detail message
     */
    public NoSuchEnumException(String s) {
        super(s);
    }
}
