/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.util.TiAppUtil;

import org.apache.commons.io.FileUtils;

import org.appcelerator.kroll.common.Log;
import android.content.Context;

/**
 * @author wei.ding
 */
public class ClearPendingUpdateTask implements Task {

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(TaskContext context) {

		Context applicationContext = context.getApplicationContext();

		// TODO Change UpdateReady to false in Application Properties

		File appDataDirectory = applicationContext.getDir(TiAppUtil.APPLICATION_DATA_DIRECTORY_KEY, Context.MODE_PRIVATE);
		File backupDirectory = new File(appDataDirectory, context.getStringValue(TaskContext.Key.BACKUP_DIRECTORY, TaskContext.Key.BACKUP_DIRECTORY));
		try {
			FileUtils.deleteDirectory(backupDirectory);
			return true;
		} catch (IOException e) {
			Log.e("ClearPendingUpdateTask", "Failed to delete '" + backupDirectory + "'.", e);
		}
		return false;
	}

}
