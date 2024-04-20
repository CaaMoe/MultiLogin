package moe.caa.multilogin.loader.exception;

import java.io.IOException;

public class LibraryLoadFailedException extends IOException {
    public LibraryLoadFailedException(String message) {
        super(message);
    }

    public LibraryLoadFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
