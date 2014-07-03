/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import ma.car.tishadow.bundle.update.ApplicationState;
import ma.car.tishadow.bundle.update.util.TiAppUtil;

import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiProperties;

import android.content.Context;

/**
 * The bundle update request's context.
 * @author wei.ding
 */
public class RequestContext {

	private AtomicReference<ApplicationState> stateRef = new AtomicReference<ApplicationState>(ApplicationState.UNKNOWN);

	private AtomicReference<BundleUpdateProcess> processRef = new AtomicReference<BundleUpdateProcess>(BundleUpdateProcess.STARTING);

	private KrollObject javascriptContext;

	private Context applicationContext;

	private final TiProperties applicationProperties;

	private HashMap<String, Object> contextProperties;

	private File backupDirectory;

	private File patchDirectory;

	private File applicationResourcesDirectory;

	private File applicationDataDirectory;

	/**
	 * Filename of manifest file.
	 */
	public static final String MANIFEST_FILENAME = "manifest.mf";

	public RequestContext(Context context) {
		this(new TiProperties(context, TiApplication.APPLICATION_PREFERENCES_NAME, false));
		this.setApplicationContext(context);
	}

	/**
	 * @param properties
	 */
	public RequestContext(TiProperties properties) {
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

	/**
	 * Gets backup directory, also called 'standby' directory, which is used to backup current application resources, and will be updated later whenever the
	 * bundle is ready to apply. This backup directory on android looks like: "/mnt/sdcard/Android/data/[appID]/files/[standby_dir]"
	 * @return {@link File} point to backup directory on external storage.
	 */
	public File getBackupDirectory() {
		if (this.backupDirectory == null) {
			String backupProp = (String) this.getContextProperties().get(RequestContext.Key.BACKUP_DIRECTORY);
			this.backupDirectory = new File(this.getApplicationContext().getExternalFilesDir(null), backupProp);
		}
		return this.backupDirectory;
	}

	/**
	 * Gets patch directory, also called 'bundle-decompress' directory, which is used to extract new bundle resources, and will be updated to backup directory.
	 * This directory on android looks like: "/mnt/sdcard/Android/data/[appID]/files/[bundle_decompress_dir]"
	 * @return
	 */
	public File getPatchDirectory() {
		if (this.patchDirectory == null) {
			String directorySeting = (String) this.getContextProperties().get(RequestContext.Key.BUNDLE_DECOMPRESS_DIRECTORY);
			this.patchDirectory = new File(this.getApplicationContext().getExternalFilesDir(null), directorySeting);
		}
		return this.patchDirectory;
	}

	/**
	 * Gets application resources directory, where you can find all javascript resources to execute in current application. This directory on android looks
	 * like: "/data/data/[appID]/appData/[app_name]/"
	 * @return
	 */
	public File getApplicationResourcesDirectory() {
		if (this.applicationResourcesDirectory == null) {
			String directorySeting = (String) this.getContextProperties().get(RequestContext.Key.APP_NAME);
			this.applicationResourcesDirectory = new File(this.applicationDataDirectory, directorySeting);
		}
		return this.applicationResourcesDirectory;
	}

	/**
	 * Gets current application's internal root data directory, where you can find all resources(javascript, caches, databases, and etc.). This directory on
	 * android looks like: "/data/data/[appID]/appData/"
	 * @return
	 */
	public File getApplicationDataDirectory() {
		if (this.applicationDataDirectory == null) {
			this.applicationDataDirectory = this.getApplicationContext().getDir(TiAppUtil.APPLICATION_DATA_DIRECTORY_KEY, Context.MODE_PRIVATE);
		}
		return this.applicationDataDirectory;
	}

	/**
	 * Gets specific bundle's manifest by the specific directory.
	 * @param directory
	 * @return
	 */
	public File getBundleManifest(File directory) {
		return new File(directory, RequestContext.MANIFEST_FILENAME);
	}

	public static class Key {

		/**
		 * The key to get app_name directory, where all current effective javascript files are.
		 */
		public static final String APP_NAME = "app_name";

		/**
		 * The key to get backup directory, which is used to backup current application, in case to recovery current version because any reason causes the
		 * bundle update fails later.
		 */
		public static final String BACKUP_DIRECTORY = "standby_dir";

		/**
		 * The key to a directory which is used to decompress latest bundle download from specific server.
		 */
		public static final String BUNDLE_DECOMPRESS_DIRECTORY = "bundle_decompress_dir";

		/**
		 * Update type: must be either 'feature_toggle' or 'dev_update'.
		 */
		public static final String UPDATE_TYPE = "update_type";

		/**
		 * The key to get latest bundle version from server
		 */
		public static final String LATEST_BUNDLE_VERSION = "latest_bundle_version";

		/**
		 * The key to a remote URL to download the latest bundle
		 */
		public static final String BUNDLE_DOWNLOAD_URL = "bundle_download_url";

		// Internal Generated keys
		/**
		 * Internal key for downloading bundle's reference id, which is retrieved from android DownlaodManager, and can be used later after equeue method is
		 * returned.
		 */
		public static final String DOWNLOADING_BUNDLE_REFID = "__downloading_bundle_refid";

		/**
		 * Internal key to get filename for the bundle which will be download shortly.
		 */
		public static final String DOWNLOAD_DESTINATION_FILENAME = "__download_destination_filename";
	}
}
