/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiProperties;

/**
 * TiShadow Bundle Update Manager
 * @author wei.ding
 */
public class BundleUpdateManager implements Task {

	private final SeriesTask taskChain;

	public BundleUpdateManager() {
		super();

		this.taskChain = new SeriesTask("BundleUpdateWorkflowTask");
		this.initial();
	}

	private void initial() {
		SeriesTask downloadAndDecompressBundleTask = new SeriesTask().addToQueue(new DownloadBundleTask()).addToQueue(new DecompressBundleTask());
		ParallelTask prepareToApplyBundleTask = new ParallelTask(downloadAndDecompressBundleTask, new BackupAppTask()) {

			private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

			/*
			 * (non-Javadoc)
			 * @see ma.car.tishadow.bundle.update.tasks.ParallelTask#execute(ma.car.tishadow.bundle.update.RequestProxy)
			 */
			@Override
			public boolean execute(RequestProxy context) {
				boolean result = super.execute(context);
				if (!result) {
					context.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
				}
				return result;
			}

		};
		// Critical Path: Check Bundle -> Download Bundle -> Decompress Bundle -> Apply to standby -> Apply to be online if force update required.
		// -> Clone App to standby _|
		this.taskChain.addToQueue(new CheckBundleTask()).addToQueue(prepareToApplyBundleTask);
		this.taskChain.addToQueue(new ApplyPatchTask()).addToQueue(new PostApplyPatchTask());
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.i("BundleUpdateManager", "Start executing bundle update tasks...");
		boolean result = this.taskChain.execute(context);
		Log.i("BundleUpdateManager", "Bundle update tasks have been executed completely.");
		return result;
	}

	/**
	 * Checks if the latest bundle has been applied.
	 * @param context the task context by request
	 * @return true if applied, otherwise false
	 */
	public static boolean isLatestBundleApplied(RequestProxy context) {
		TiProperties applicationProperties = context.getApplicationProperties();
		long currentBundleVersion = applicationProperties.getInt(PropertyKey.CURRENT_BUNDLE_VERSION, -1);
		long latestBundleVersion = context.getLatestBundleVerion();
		return latestBundleVersion <= currentBundleVersion;
	}

	/**
	 * Checks if the latest bundle has been applied.
	 * @param context the task context by request
	 * @return true if applied, otherwise false
	 */
	public static boolean isLatestBundleReadyForApply(RequestProxy context) {
		TiProperties applicationProperties = context.getApplicationProperties();
		long readyForApplyVersion = applicationProperties.getInt(PropertyKey.UPDATE_VERSION_KEY, -1);
		long latestBundleVersion = context.getLatestBundleVerion();
		return latestBundleVersion == readyForApplyVersion;
	}

	/**
	 * Checks if the latest bundle need to be download.
	 * @param context task context by request.
	 * @return true if need, otherwise false.
	 */
	public static boolean isBundleDownloadRequired(RequestProxy context) {
		String updateType = (String) context.getRequestProperties().get(RequestProxy.Key.UPDATE_TYPE);
		if ("dev_update".equalsIgnoreCase(updateType)) {
			return true;
		}
		if ("feature_toggle".equalsIgnoreCase(updateType) && !isLatestBundleApplied(context) && !isLatestBundleReadyForApply(context)) {
			return true;
		}
		return false;
	}

}
