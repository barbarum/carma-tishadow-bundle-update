/**
 * 
 */
package ma.car.tishadow.bundle.update.util;

/**
 * Manifest parse exception.
 * @author wei.ding
 */
public class ManifestParseException extends Exception {

	private static final long serialVersionUID = 1L;

	public ManifestParseException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ManifestParseException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ManifestParseException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ManifestParseException(Throwable cause) {
		super(cause);
	}

}
