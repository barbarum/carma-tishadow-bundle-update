/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;
import android.util.Log;

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
			if (!BundleUpdateManager.isLatestBundleApplied(context)) {
				new ClearPendingUpdateTask().execute(context);
			}
			return true;
		}
		if ("dev_update".equalsIgnoreCase(updateType)) {
			new ClearPendingUpdateTask().execute(context);
			return true;
		}
		return false;
	}

	private void logBundleInfo(RequestProxy context, String updateType) {
		if (!Log.isLoggable(TAG, Log.VERBOSE)) {
			return;
		}
		int currentBundleVersion = context.getApplicationProperties().getInt(PropertyKey.CURRENT_BUNDLE_VERSION, -1);
		Object latestBundleVersion = context.getRequestProperty(RequestProxy.Key.LATEST_BUNDLE_VERSION);
		StringBuilder builder = new StringBuilder();
		builder.append("===========================App Bundle Info===========================");
		builder.append("\nUpdate type: " + updateType);
		builder.append("\nLocal bundle: " + currentBundleVersion);
		builder.append("\nLatest bundle:" + latestBundleVersion);
		builder.append("\nAppData Dir: " + context.getApplicationDataDirectory());
		builder.append("\nTemp Dir: " + context.getExternalApplicationTemporaryDirectory());
		builder.append("\n=====================================================================");
		Log.v(TAG, builder.toString());
	}
}
