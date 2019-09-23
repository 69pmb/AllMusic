package pmb.music.AllMusic.utils;

/**
 * Runtime exception of the application.
 *
 * @author pmbroca
 */
public class MinorException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructeur avec message.
     *
     * @param message le message d'erreur
     */
    public MinorException(String message) {
        super(message);
    }

    /**
     * Constructeur avec message et throwable.
     *
     * @param message le message d'erreur
     * @param throwable la cause de l'erreur
     */
    public MinorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Constructeur parent vide.
     */
    public MinorException() {
        super();
    }
}
