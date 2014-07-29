/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.BundleUpdateState;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.ManifestUtil;

import org.apache.commons.io.FileUtils;
import org.appcelerator.kroll.common.Log;

// import org.appcelerator.kroll.common.Log;

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

		try {
			ManifestUtil.PatchInfo patchInfo = getPatchInfo(context);
			if (patchInfo == null || patchInfo.isEmpty()) {
				Log.i(TAG, "Already up-to-date!");
				return false;
			}
			applyPatch(context, patchInfo);

			// Update states into application properties, which are used by tishadow javascript side to retrieve update states and related information.
			context.setBundleUpdateInfo();
			context.markedBundleUpdateStateTo(BundleUpdateState.READY_FOR_APPLY);
			
			Log.d(TAG, "Apply Patch done.");
			return true;
		} catch (Exception e) {
			context.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
			Log.e(TAG, "Failed to apply patch!", e);
		}
		return false;
	}

	private ManifestUtil.PatchInfo getPatchInfo(RequestProxy context) throws IOException {
		File oldManifest = ManifestUtil.getBundleManifest(context.getBackupDirectory());
		File newManifest = ManifestUtil.getBundleManifest(context.getPatchDirectory());
		return ManifestUtil.compareManifest(oldManifest, newManifest);
	}

	private void applyPatch(RequestProxy context, ManifestUtil.PatchInfo patch) throws IOException {
		for (String item : patch.getFilesToDelete()) {
			File file = new File(context.getBackupDirectory(), item);
			file.delete();
		}
		for (String item : patch.getFilesToAdd()) {
			File source = new File(context.getPatchDirectory(), item);
			File dest = new File(context.getBackupDirectory(), item);
			FileUtils.copyFile(source, dest);
		}
		for (String item : patch.getFilesToUpdate()) {
			File source = new File(context.getPatchDirectory(), item);
			File dest = new File(context.getBackupDirectory(), item);
			FileUtils.copyFile(source, dest, false);
		}
		FileUtils.copyFile(ManifestUtil.getBundleManifest(context.getPatchDirectory()), ManifestUtil.getBundleManifest(context.getBackupDirectory()), false);
		FileUtils.deleteDirectory(context.getPatchDirectory());
	}
}
