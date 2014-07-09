/**
 * 
 */
package ma.car.tishadow.bundle.update;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import ma.car.tishadow.bundle.update.tasks.BundleUpdateState;
import ma.car.tishadow.bundle.update.util.ManifestParseException;
import ma.car.tishadow.bundle.update.util.ManifestUtil;
import ma.car.tishadow.bundle.update.util.TiAppUtil;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiProperties;

import android.content.Context;

/**
 * The bundle update request's context.
 * @author wei.ding
 */
@Kroll.proxy(creatableInModule = TishadowBundleUpdateModule.class)
public class RequestProxy extends KrollProxy {

	private static final String TEMPORARY_DIRECTORY_NAME = "tmp";

	private AtomicReference<ApplicationState> stateRef = new AtomicReference<ApplicationState>(ApplicationState.UNKNOWN);

	private AtomicReference<BundleUpdateState> updateStateRef = new AtomicReference<BundleUpdateState>(BundleUpdateState.STARTING);

	private KrollObject javascriptContext;

	private Context applicationContext;

	private TiProperties applicationProperties;

	private HashMap<String, Object> properties;

	private File backupDirectory;

	private File patchDirectory;

	private File applicationResourcesDirectory;

	private File applicationDataDirectory;

	private File externalApplicationBaseDirectory;

	private File externalApplicationTemporaryDirectory;

	private BundleUpdateStateListener onBundleUpdateStateChangedListener;

	public RequestProxy(TiContext tiContext) {
		this(tiContext.getActivity());
	}

	public RequestProxy(Context context) {
		this(new TiProperties(context, TiApplication.APPLICATION_PREFERENCES_NAME, false));
		this.setApplicationContext(context);
	}

	/**
	 * @param properties
	 */
	public RequestProxy(TiProperties properties) {
		super();
		this.applicationProperties = properties;
	}

	/*
	 * (non-Javadoc)
	 * @see org.appcelerator.kroll.KrollProxy#handleCreationDict(org.appcelerator.kroll.KrollDict)
	 */
	@Override
	public void handleCreationDict(KrollDict dict) {
		super.handleCreationDict(dict);
		this.setRequestProperties(dict);
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
	public HashMap<String, Object> getRequestProperties() {
		return properties;
	}

	/**
	 * Gets context property by key.
	 * @param key
	 * @return
	 */
	public Object getRequestProperty(String key) {
		return properties.get(key);
	}

	/**
	 * Gets context callback by specific key.
	 * @param key
	 * @return
	 */
	public KrollFunction getRequestCallback(String key) {
		Object function = this.properties.get(key);
		return function == null || !(function instanceof KrollFunction) ? null : (KrollFunction) function;
	}

	/**
	 * @param properties the contextProperties to set
	 */
	public void setRequestProperties(HashMap<String, Object> properties) {
		this.properties = properties;
	}

	/**
	 * Marks current bundle update process to currentState.
	 * @param currentState current state.
	 */
	public void markedBundleUpdateStateTo(BundleUpdateState currentState) {
		BundleUpdateState previousState = this.updateStateRef.getAndSet(currentState);
		if (this.onBundleUpdateStateChangedListener != null) {
			this.onBundleUpdateStateChangedListener.onBundleUpdateStateChanged(previousState, currentState, this);
		}
	}

	/**
	 * Gets current bundle update process.
	 * @return
	 */
	public BundleUpdateState getCurrentBundleUpdateProcess() {
		return this.updateStateRef.get();
	}

	/**
	 * Gets current bundle update process.
	 * @return
	 */
	@Kroll.getProperty
	@Kroll.method
	public String getUpdateState() {
		return this.getCurrentBundleUpdateProcess().toString();
	}

	/**
	 * Gets backup directory, also called 'standby' directory, which is used to backup current application resources, and will be updated later whenever the
	 * bundle is ready to apply. This backup directory on android looks like: "/data/data/[appID]/app_appData/[standby_dir]"
	 * @return {@link File} point to backup directory on external storage.
	 */
	public File getBackupDirectory() {
		if (this.backupDirectory == null) {
			String backupDirectoryName = (String) this.getRequestProperty(RequestProxy.Key.BACKUP_DIRECTORY);
			this.backupDirectory = new File(this.getApplicationDataDirectory(), backupDirectoryName);
		}
		return this.backupDirectory;
	}

	private File getExternalApplicationBaseDirectory() {
		if (this.externalApplicationBaseDirectory == null) {
			File externalCacheDirectory = this.getApplicationContext().getExternalCacheDir();
			this.externalApplicationBaseDirectory = externalCacheDirectory.getParentFile();
		}
		return this.externalApplicationBaseDirectory;
	}

	/**
	 * Gets current application's external temporary directory, which is used to temporary caches, or hard drive manipulation(e.g bundle update).
	 * @return
	 */
	public File getExternalApplicationTemporaryDirectory() {
		if (this.externalApplicationTemporaryDirectory == null) {
			File baseDir = this.getExternalApplicationBaseDirectory();
			if (baseDir == null) {
				return null;
			}
			File tempDir = new File(baseDir, TEMPORARY_DIRECTORY_NAME);
			tempDir.mkdirs();
			this.externalApplicationTemporaryDirectory = tempDir.exists() ? tempDir : null;
		}
		return this.externalApplicationTemporaryDirectory;
	}

	/**
	 * Gets patch directory, also called 'bundle-decompress' directory, which is used to extract new bundle resources, and will be updated to backup directory.
	 * This directory on android looks like: "/mnt/sdcard/Android/data/[appID]/tmp/[bundle_decompress_dir]"
	 * @return
	 */
	public File getPatchDirectory() {
		if (this.patchDirectory == null) {
			String directorySeting = (String) this.getRequestProperties().get(RequestProxy.Key.BUNDLE_DECOMPRESS_DIRECTORY);
			this.patchDirectory = new File(getExternalApplicationTemporaryDirectory(), directorySeting);
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
			String directorySeting = (String) this.getRequestProperties().get(RequestProxy.Key.APP_NAME);
			this.applicationResourcesDirectory = new File(this.getApplicationDataDirectory(), directorySeting);
		}
		return this.applicationResourcesDirectory;
	}

	/**
	 * Gets application resources' base directory name.
	 * @return
	 */
	public String getApplicationResourcesDirectoryName() {
		return (String) this.getRequestProperties().get(RequestProxy.Key.APP_NAME);
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
	 * @return the onBundleUpdateStateChangedListener
	 */
	public BundleUpdateStateListener getOnBundleUpdateStateChangedListener() {
		return onBundleUpdateStateChangedListener;
	}

	/**
	 * @param onBundleUpdateStateChangedListener the onBundleUpdateStateChangedListener to set
	 */
	public void setOnBundleUpdateStateChangedListener(BundleUpdateStateListener onBundleUpdateStateChangedListener) {
		this.onBundleUpdateStateChangedListener = onBundleUpdateStateChangedListener;
	}

	/**
	 * Gets latest bundle version from request parameter.
	 * @return the value of 'latest_bundle_version' in request parameters, -1 if no such a parameter.
	 * @throws NumberFormatException - if the value of 'latest_bundle_version' is not a number.
	 */
	public long getLatestBundleVerion() {
		Object latestBundleVersion = this.getRequestProperty(RequestProxy.Key.LATEST_BUNDLE_VERSION);
		return latestBundleVersion == null ? -1 : Long.parseLong(latestBundleVersion.toString());
	}

	/**
	 * Clear bundle update information.
	 */
	public void clearBundleUpdateInfo() {
		getApplicationProperties().setBool(PropertyKey.UPDATE_READY_SIGN_KEY, false);
		getApplicationProperties().removeProperty(PropertyKey.UPDATE_VERSION_KEY);
	}

	/**
	 * Set bundle update information from the manifest of standby directory.
	 * @throws IOException
	 * @throws ManifestParseException
	 */
	public void setBundleUpdateInfo() throws IOException, ManifestParseException {
		getApplicationProperties().setBool(PropertyKey.UPDATE_READY_SIGN_KEY, true);
		int updateVersion = (int) ManifestUtil.readBundleVersion(ManifestUtil.getBundleManifest(getBackupDirectory()));
		getApplicationProperties().setInt(PropertyKey.UPDATE_VERSION_KEY, updateVersion);
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

		/**
		 * The key to start downloading bundle callback.
		 */
		public static final String ON_BUNDLE_DOWNLOADING_CALLBACK = "onBundleDownloading";

		/**
		 * The key to the callback KrollFunction, where javascript side can handle their own logic(change bundle update progressing bar UI e.g).
		 */
		public static final String ON_BUNDLE_DOWNLOAD_CALLBACK = "onBundleDownload";

		/**
		 * The key to the callback KrollFunction when the download bundle has been extracted.
		 */
		public static final String ON_BUNDLE_EXTRACTED_CALLBACK = "onBundleExtracted";

		/**
		 * The key to the callback KrollFunction when the bundle is ready for apply.
		 */
		public static final String ON_BUNDLE_READY_FOR_APPLY_CALLBACK = "onBundleReadyForApply";

		/**
		 * The key to the callback KrollFunction when the extracted bundle is applying to backup/standby directory.
		 */
		public static final String ON_BUNDLE_APPLYING_CALLBACK = "onBundleApplying";

		/**
		 * The key to the callback krollFunction when the bundle has been applied completely.
		 */
		public static final String ON_BUNDLE_APPLIED_CALLBACK = "onBundleApplied";

		/**
		 * The key to the callback KrollFunction whenever the bundle process has been changed.
		 */
		public static final String ON_STATE_CHANGED_CALLBACK = "onStateChanged";

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
