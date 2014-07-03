/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.util.TiAppUtil;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;

import org.apache.commons.io.FileUtils;
import org.appcelerator.kroll.common.Log;

import android.content.Context;

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
	public boolean execute(RequestContext context) {

		Context applicationContext = context.getApplicationContext();

		context.getApplicationProperties().setBool(PropertyKey.UPDATE_READY_KEY, false);

		File appDataDirectory = applicationContext.getDir(TiAppUtil.APPLICATION_DATA_DIRECTORY_KEY, Context.MODE_PRIVATE);
		File backupDirectory = new File(appDataDirectory, context.getApplicationProperties().getString(RequestContext.Key.BACKUP_DIRECTORY, RequestContext.Key.BACKUP_DIRECTORY));

		Log.d(TAG, "AppDataDirectory : " + appDataDirectory + ", BackupDirectory : " + backupDirectory, Log.DEBUG_MODE);
		try {
			FileUtils.deleteDirectory(backupDirectory);
			Log.i(TAG, "Clear pending update has been done.");
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Failed to delete '" + backupDirectory + "'.", e);
		}
		return false;
	}

}
