package pmb.music.AllMusic.utils;


public class MyException extends Exception {
    private static final long serialVersionUID = 1L;
    
    public MyException(String message) {
        super(message);
    }
    
    public MyException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MyException() {
        super();
    }

    public MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public MyException(Throwable cause) {
        super(cause);
    }
}
