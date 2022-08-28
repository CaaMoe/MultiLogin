package moe.caa.multilogin.api.util.reflect;

/**
 * Signals that the class doesn't have a constructor of a specified name.
 */
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
