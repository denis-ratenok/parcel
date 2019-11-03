package parser;

public class NotCorrectProductQuantityException extends Exception {

    public NotCorrectProductQuantityException() {}

    public NotCorrectProductQuantityException(String message) {
        super(message);
    }
}
