package bk.exception;

public class EmailAlreadyExistsException extends Exception {

    public EmailAlreadyExistsException() {
        super("Email đã tồn tại trong hệ thống");
    }

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}