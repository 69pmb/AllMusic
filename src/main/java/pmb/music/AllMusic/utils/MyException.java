package pmb.music.AllMusic.utils;

/**
 * La classe d'exception du programme.
 * 
 * @author pmbroca
 */
public class MyException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructeur avec message.
	 * 
	 * @param message le message d'erreur
	 */
	public MyException(String message) {
		super(message);
	}

	/**
	 * Constructeur avec message et throwable.
	 * 
	 * @param message le message d'erreur
	 * @param throwable la cause de l'erreur
	 */
	public MyException(String message, Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Constructeur parent vide.
	 */
	public MyException() {
		super();
	}

	/**
	 * Constructeur.
	 * 
	 * @param message le message d'erreur
	 * @param cause la cause de l'erreur
	 * @param enableSuppression whether or not suppression is enabled or disabled
	 * @param writableStackTrace whether or not the stack trace should be writable
	 */
	public MyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * Constructeur avec le throwable.
	 * 
	 * @param cause la cause de l'erreur
	 */
	public MyException(Throwable cause) {
		super(cause);
	}
}
