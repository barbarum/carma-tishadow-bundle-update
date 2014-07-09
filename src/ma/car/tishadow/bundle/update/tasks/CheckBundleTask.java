/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import ma.car.tishadow.bundle.update.BundleUpdateState;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;

import org.appcelerator.kroll.common.Log;

/**
 * @author wei.ding
 */
public class CheckBundleTask implements Task {

	private static final String TAG = "CheckBundleTask";

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.v(TAG, "Start checking bundle task...");
		Boolean result = doExecute(context);
		context.markedBundleUpdateStateTo(result ? BundleUpdateState.CHECKED : BundleUpdateState.INTERRUPTED);
		Log.d(TAG, "Check bundle task done.");
		return result;
	}

	private boolean doExecute(RequestProxy context) {
		String updateType = (String) context.getRequestProperty(RequestProxy.Key.UPDATE_TYPE);
		logBundleInfo(context, updateType);
		if ("feature_toggle".equalsIgnoreCase(updateType)) {
			if (!BundleUpdateManager.isLatestBundleApplied(context) && !BundleUpdateManager.isLatestBundleReadyForApply(context)) {
				new ClearPendingUpdateTask().execute(context);
				return true;
			}
		}
		if ("dev_update".equalsIgnoreCase(updateType)) {
			new ClearPendingUpdateTask().execute(context);
			return true;
		}
		return false;
	}

	private void logBundleInfo(RequestProxy context, String updateType) {
		if (!Log.isDebugModeEnabled()) {
			return;
		}
		int currentBundleVersion = context.getApplicationProperties().getInt(PropertyKey.CURRENT_BUNDLE_VERSION, -1);
		StringBuilder builder = new StringBuilder();
		builder.append("==================App Bundle Info==================");
		builder.append("\nUpdate type: " + updateType);
		builder.append("\nLocal bundle: " + currentBundleVersion);
		builder.append("\nLatest bundle:" + context.getLatestBundleVerion());
		builder.append("\nAppData Dir: " + context.getApplicationDataDirectory());
		builder.append("\nAppTmp Dir: " + context.getExternalApplicationTemporaryDirectory());
		builder.append("\nApplication Resources Dir: " + context.getApplicationResourcesDirectory());
		builder.append("\nBackup Dir: " + context.getBackupDirectory());
		builder.append("\nBundle Dir: " + context.getPatchDirectory());
		builder.append("\n=====================================================================");
		Log.v(TAG, builder.toString());
	}
}
