/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import ma.car.tishadow.bundle.update.ApplicationState;

import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.content.Context;

/**
 * @author wei.ding
 */
public class TaskContext {

	private AtomicReference<ApplicationState> stateRef = new AtomicReference<ApplicationState>(ApplicationState.UNKNOWN);

	private AtomicReference<BundleUpdateProcess> processRef = new AtomicReference<BundleUpdateProcess>(BundleUpdateProcess.STARTING);

	private KrollObject javascriptContext;

	private Context applicationContext;

	private final TiProperties applicationProperties;

	private HashMap<String, Object> contextProperties;

	public TaskContext(Context context) {
		this(new TiProperties(context, TiApplication.APPLICATION_PREFERENCES_NAME, false));
		this.setApplicationContext(context);
	}

	/**
	 * @param properties
	 */
	public TaskContext(TiProperties properties) {
		super();
		this.applicationProperties = properties;
	}

	/**
	 * Changes the application's bundle state.
	 * @param state
	 */
	public void setApplicationState(ApplicationState state) {
		this.stateRef.set(state);
	}

	/**
	 * @return the javascriptContext
	 */
	public KrollObject getJavascriptContext() {
		return javascriptContext;
	}

	/**
	 * @param javascriptContext the javascriptContext to set
	 */
	public void setJavascriptContext(KrollObject javascriptContext) {
		this.javascriptContext = javascriptContext;
	}

	/**
	 * @return the applicationContext
	 */
	public Context getApplicationContext() {
		return applicationContext;
	}

	/**
	 * @param applicationContext the applicationContext to set
	 */
	public void setApplicationContext(Context applicationContext) {
		this.applicationContext = applicationContext;
	}

	/**
	 * @return the properties
	 */
	public TiProperties getApplicationProperties() {
		return applicationProperties;
	}

	/**
	 * @return the contextProperties
	 */
	public HashMap<String, Object> getContextProperties() {
		return contextProperties;
	}

	/**
	 * @param contextProperties the contextProperties to set
	 */
	public void setContextProperties(HashMap<String, Object> contextProperties) {
		this.contextProperties = contextProperties;
	}

	/**
	 * Marks current bundle update process to currentProcess.
	 * @param currentProcess current process.
	 */
	public void markedBundleUpdateProcessTo(BundleUpdateProcess currentProcess) {
		this.processRef.set(currentProcess);
	}

	/**
	 * Gets current bundle update process.
	 * @return
	 */
	public BundleUpdateProcess getCurrentBundleUpdateProcess() {
		return this.processRef.get();
	}

	public static class Key {

		public static final String BACKUP_DIRECTORY = "standby_dir";

		public static final String UPDATE_TYPE = "update_type";

		/**
		 * The key to get latest bundle version from server
		 */
		public static final String LATEST_BUNDLE_VERSION = "latest_bundle_version";

		/**
		 * The key to get the version number of the bundle which is working now.
		 */
		public static final String CURRENT_BUNDLE_VERSION = "current_bundle_version";

		// Internal Generated keys
		/**
		 * Internal key for downloading bundle's reference id, which is retrieved from android DownlaodManager, and can be used later after equeue method is
		 * returned.
		 */
		public static final String DOWNLOADING_BUNDLE_REFID = "__downloading_bundle_refid";
	}
}
