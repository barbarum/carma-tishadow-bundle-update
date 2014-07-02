/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.io.IOException;

import ma.car.tishadow.bundle.update.util.ZipUtil;

import org.apache.commons.io.FileUtils;
import org.appcelerator.kroll.common.Log;

import android.content.Context;

/**
 * Represents a single task to decompress latest bundle.
 * @author wei.ding
 */
public class DecompressBundleTask implements Task {

	private static final String TAG = "DecompressBundleTask";

	/*
	 * Extract latest download bundle into decompression directory, and delete download file as well.
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(TaskContext context) {
		Context applicationContext = context.getApplicationContext();
		String bundleDecompressDirectory = (String) context.getContextProperties().get(TaskContext.Key.BUNDLE_DECOMPRESS_DIRECTORY);
		String filename = (String) context.getContextProperties().get(TaskContext.Key.DOWNLOAD_DESTINATION_FILENAME);

		File compressionFile = new File(applicationContext.getExternalFilesDir(null), filename);
		File decompressDirectory = new File(applicationContext.getExternalFilesDir(null), bundleDecompressDirectory);

		Log.i(TAG, "Extracting latest bundle '" + compressionFile + "' into '" + decompressDirectory + "'...");
		if (!compressionFile.exists()) {
			Log.i(TAG, "Latest bundle '" + compressionFile + " doesn't exist. ");
			return false;
		}
		if (decompressDirectory.exists()) {
			Log.d(TAG, "Clean up previous extraction mess...");
			try {
				FileUtils.cleanDirectory(decompressDirectory);
			} catch (IOException e) {
				Log.e(TAG, "Failed to clean directory '" + decompressDirectory + "', see more detail from exception stack.", e);
				return false;
			}
		} else {
			decompressDirectory.mkdir();
		}
		ZipUtil.decompress(decompressDirectory.getAbsolutePath(), compressionFile.getAbsolutePath(), true);
		compressionFile.delete();
		Log.i(TAG, "Extract latest bundle done.");
		return true;
	}
}
