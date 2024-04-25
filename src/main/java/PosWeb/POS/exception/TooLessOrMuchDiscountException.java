package PosWeb.POS.exception;

public class TooLessOrMuchDiscountException extends IllegalArgumentException{

    public TooLessOrMuchDiscountException() {
        super();
    }

    public TooLessOrMuchDiscountException(String message) {
        super(message);
    }

    public TooLessOrMuchDiscountException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooLessOrMuchDiscountException(Throwable cause) {
        super(cause);
    }
}
