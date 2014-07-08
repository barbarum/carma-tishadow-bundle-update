/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.ManifestUtil;

import org.apache.commons.io.FileUtils;

import android.util.Log;

/**
 * Represents a single task to apply patch into backup directory.
 * @author wei.ding
 */
public class ApplyPatchTask implements Task {

	private static final String TAG = "ApplyPatchTask";

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.v(TAG, "Starting applying patch task...");

		context.markedBundleUpdateStateTo(BundleUpdateState.APPLYING);

		ManifestUtil.Patch patch;
		try {
			File patchDirectory = context.getPatchDirectory();
			File backupDirectory = context.getBackupDirectory();
			File oldManifest = context.getBundleManifest(backupDirectory);
			File newManifest = context.getBundleManifest(patchDirectory);

			patch = ManifestUtil.compareManifest(oldManifest, newManifest);

			if (patch == null || patch.isEmpty()) {
				Log.i(TAG, "Already up-to-date!");
				return false;
			}
			for (String item : patch.getFilesToDelete()) {
				File file = new File(backupDirectory, item);
				file.delete();
			}
			for (String item : patch.getFilesToAdd()) {
				File source = new File(patchDirectory, item);
				File dest = new File(backupDirectory, item);
				FileUtils.copyFile(source, dest);
			}
			for (String item : patch.getFilesToUpdate()) {
				File source = new File(patchDirectory, item);
				File dest = new File(backupDirectory, item);
				FileUtils.copyFile(source, dest, false);
			}
			FileUtils.copyFile(newManifest, oldManifest, false);
			FileUtils.deleteDirectory(context.getPatchDirectory());

			Log.d(TAG, "Apply Patch done.");
			return true;
		} catch (IOException e) {
			context.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
			Log.e(TAG, "Failed to apply patch!", e);
		}
		return false;
	}
}
