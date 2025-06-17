package bk.exception;

public class InvalidCredentialsException extends Exception {

    public InvalidCredentialsException() {
        super("Thông tin đăng nhập không chính xác");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}