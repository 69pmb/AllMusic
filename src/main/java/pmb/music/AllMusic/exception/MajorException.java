package pmb.music.AllMusic.exception;

/**
 * Checked exception of the application, for usual or big exceptions.
 *
 * @author pmbroca
 */
public class MajorException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Constructeur avec message.
     *
     * @param message le message d'erreur
     */
    public MajorException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et throwable.
     *
     * @param message le message d'erreur
     * @param throwable la cause de l'erreur
     */
    public MajorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructeur parent vide.
     */
    public MajorException() {
        super();
    }
}
