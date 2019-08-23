package pmb.music.AllMusic.utils;

/**
 * La classe d'exception du programme.
 * 
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
}
