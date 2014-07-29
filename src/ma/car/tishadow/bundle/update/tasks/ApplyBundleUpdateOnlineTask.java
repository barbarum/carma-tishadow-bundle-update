package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.BundleUpdateState;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.ManifestParseException;
import ma.car.tishadow.bundle.update.util.ManifestUtil;
import ma.car.tishadow.bundle.update.util.TiAppUtil;

import org.apache.commons.io.FileUtils;
import org.appcelerator.kroll.common.Log;

/**
 * Apply bundle update into current application.
 * @author wei.ding
 */
public class ApplyBundleUpdateOnlineTask implements Task {

	private static final String TAG = "ApplyUpdateTask";

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy request) {
		Log.v(TAG, "Starting applying update task...");
		try {
			apply(request);
			request.clearBundleUpdateInfo();
			request.getApplicationProperties().setInt(TiAppUtil.PropertyKey.CURRENT_BUNDLE_VERSION, readAppBundleVersion(request));
			request.markedBundleUpdateStateTo(BundleUpdateState.APPLIED);
			Log.d(TAG, "Applying update done.");
			return true;
		} catch (Exception e) {
			request.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
			Log.e("ApplyUpdateTask", "Failed to apply bundle update into main application.", e);
		}
		return false;
	}

	private void apply(RequestProxy context) throws IOException {

		FileUtils.deleteDirectory(context.getApplicationResourcesDirectory());

		File backupDirectory = new File(context.getBackupDirectory().getAbsolutePath());
		File applicationResourceDirectory = context.getApplicationResourcesDirectory();
		if (backupDirectory.renameTo(applicationResourceDirectory)) {
			Log.d(TAG, backupDirectory + " has been renamed to " + applicationResourceDirectory);
		} else {
			Log.e(TAG, "Failed to rename to " + applicationResourceDirectory + " from " + backupDirectory + ", checking directories...");
			Log.e(TAG, "standby directory exists ? " + backupDirectory.exists());
			Log.e(TAG, "application resource directory exists ? " + applicationResourceDirectory.exists());
		}

	}

	private int readAppBundleVersion(RequestProxy request) throws IOException, ManifestParseException {
		return (int) ManifestUtil.readBundleVersion(ManifestUtil.getBundleManifest(request.getApplicationResourcesDirectory()));
	}
}
