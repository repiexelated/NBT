package net.querz.mca.entities;

public class IllegalEntityTagException extends IllegalArgumentException {
    public IllegalEntityTagException() {
        super();
    }

    public IllegalEntityTagException(String message) {
        super(message);
    }

    public IllegalEntityTagException(String message, Throwable cause) {
        super(message, cause);
    }

    public IllegalEntityTagException(Throwable cause) {
        super(cause);
    }
}
