/**
 * 
 */
package ma.car.tishadow.bundle.update.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The utility for current application.
 * @author wei.ding
 */
public class TiAppUtil {

	/**
	 * Cached thread pool.
	 */
	public static ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	/**
	 * Application data directory key.
	 */
	public static final String APPLICATION_DATA_DIRECTORY_KEY = "appData";

	/**
	 * Application's persistent properties keys
	 * @author wei.ding
	 */
	public static class PropertyKey {

		/**
		 * The key to get the version number of the bundle which is working now.
		 */
		public static final String CURRENT_BUNDLE_VERSION = "bundleVersion";

		/**
		 * Updated Version's key
		 */
		public static final String UPDATE_VERSION_KEY = "updateVersion";

		/**
		 * Update ready sign's key
		 */
		public static final String UPDATE_READY_SIGN_KEY = "updateReady";
	}

}
