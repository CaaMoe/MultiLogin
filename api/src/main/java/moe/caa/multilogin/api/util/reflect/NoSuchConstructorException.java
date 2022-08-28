package moe.caa.multilogin.api.util.reflect;

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
