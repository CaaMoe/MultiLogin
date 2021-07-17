package moe.caa.multilogin.core.exception;

public class NoSuchEnumException extends ReflectiveOperationException {

    public NoSuchEnumException() {
        super();
    }

    public NoSuchEnumException(String s) {
        super(s);
    }
}
