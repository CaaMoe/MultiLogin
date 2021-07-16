package moe.caa.multilogin.core;

public class NoSuchEnumException extends ReflectiveOperationException {

    public NoSuchEnumException() {
        super();
    }

    public NoSuchEnumException(String s) {
        super(s);
    }
}
