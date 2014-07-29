/**
 * 
 */
package ma.car.tishadow.bundle.update.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The utility for current application.
 * @author wei.ding
 */
public class TiAppUtil {

	public static final int MAXIMUM_POOL_SIZE = 8;
	/**
	 * Cached thread pool.
	 */
	public static ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, MAXIMUM_POOL_SIZE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

	/**
	 * Application data directory key.
	 */
	public static final String APPLICATION_DATA_DIRECTORY_KEY = "appdata";

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
