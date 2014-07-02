/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.content.Context;
import android.util.Log;

/**
 * Represents a single task to backup current application resource into backup directory, which will be used to compare with latest bundle contents, and apply
 * the update into this backup directory.
 * @author wei.ding
 */
public class BackupAppTask implements Task {

	private static final String TAG = "BackupAppTask";

	/*
	 * (non-javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(TaskContext context) {

		String backupProp = (String) context.getContextProperties().get(TaskContext.Key.BACKUP_DIRECTORY);
		String sourceProp = (String) context.getContextProperties().get(TaskContext.Key.APP_NAME);

		File backupDirectory = new File(context.getApplicationContext().getExternalFilesDir(null), backupProp);
		File sourceDirectory = new File(context.getApplicationContext().getDir(sourceProp, Context.MODE_PRIVATE), sourceProp);

		Log.i(TAG, "Backup application resources '" + sourceDirectory + "' -> '" + backupDirectory + "'...");
		try {
			if (backupDirectory.exists()) {
				FileUtils.cleanDirectory(backupDirectory);
			}
			if (sourceDirectory.exists()) {
				FileUtils.copyDirectory(sourceDirectory, backupDirectory);
			}
			Log.i(TAG, "Backup application resources done.");
			return true;
		} catch (IOException e) {
			Log.e(TAG, "Failed to backup application resources.", e);
		}

		return false;
	}
}
