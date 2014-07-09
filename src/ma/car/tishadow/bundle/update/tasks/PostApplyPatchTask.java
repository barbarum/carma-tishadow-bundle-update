/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.IOException;

import android.util.Log;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.ManifestParseException;
import ma.car.tishadow.bundle.update.util.ManifestUtil;

/**
 * A task is executed after applying patches.
 * @author wei.ding
 */
public class PostApplyPatchTask implements Task {

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.RequestProxy)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		try {
			boolean flag = ManifestUtil.isBundleRequireForceUpdate(ManifestUtil.getBundleManifest(context.getBackupDirectory()));
			if (flag) {
				new ApplyBundleUpdateOnlineTask().execute(context);
			}
			return true;
		} catch (IOException e) {
			Log.e("PostApplyPatchTask", "Failed to read manifest file from standby directory.", e);
		} catch (ManifestParseException e) {
			Log.e("PostApplyPatchTask", "Failed to read manifest file from standby directory.", e);
		}

		return false;
	}

}
