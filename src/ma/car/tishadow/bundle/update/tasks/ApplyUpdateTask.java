package ma.car.tishadow.bundle.update.tasks;

import java.io.IOException;

import ma.car.tishadow.bundle.update.RequestProxy;

import org.apache.commons.io.FileUtils;

import android.util.Log;

/**
 * Apply bundle update into current application.
 * @author wei.ding
 */
public class ApplyUpdateTask implements Task {

	private static final String TAG = "ApplyUpdateTask";

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.v(TAG, "Starting applying update task...");
		try {
			FileUtils.deleteDirectory(context.getApplicationResourcesDirectory());
			FileUtils.copyDirectoryToDirectory(context.getBackupDirectory(), context.getApplicationResourcesDirectory());
			FileUtils.deleteDirectory(context.getExternalApplicationTemporaryDirectory());
			context.markedBundleUpdateStateTo(BundleUpdateState.APPLYED);
			Log.d(TAG, "Applying update done.");
			return true;
		} catch (IOException e) {
			context.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
			Log.e("ApplyUpdateTask", "Failed to apply bundle update into main application.", e);
		}
		return false;
	}

}
