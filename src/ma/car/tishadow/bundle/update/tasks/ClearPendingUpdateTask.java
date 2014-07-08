/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.RequestProxy;

import org.apache.commons.io.FileUtils;

import org.appcelerator.kroll.common.Log;

/**
 * Represents a single task to clear previous pending update.
 * @author wei.ding
 */
public class ClearPendingUpdateTask implements Task {

	private static final String TAG = "ClearPendingUpdateTask";

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.v(TAG, "Start clearing pending update ...");
		File backupDirectory = context.getBackupDirectory();
		Log.i(TAG, "Clearing pending updates from " + backupDirectory);
		try {
			FileUtils.deleteDirectory(backupDirectory);
			Log.d(TAG, "Clear pending update has been done.");
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Failed to delete '" + backupDirectory + "'.", e);
		}
		return false;
	}
}
