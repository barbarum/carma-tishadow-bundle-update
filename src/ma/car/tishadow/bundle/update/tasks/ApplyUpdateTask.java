package ma.car.tishadow.bundle.update.tasks;

import java.io.IOException;

import org.apache.commons.io.FileUtils;

import android.util.Log;

/**
 * Apply bundle update into current application.
 * @author wei.ding
 */
public class ApplyUpdateTask implements Task {

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestContext context) {
		try {
			FileUtils.deleteDirectory(context.getApplicationResourcesDirectory());
			FileUtils.copyDirectoryToDirectory(context.getBackupDirectory(), context.getApplicationResourcesDirectory());
			return true;
		} catch (IOException e) {
			Log.e("ApplyUpdateTask", "Failed to apply bundle update into main application.", e);
		}
		return false;
	}

}
