/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.HashMap;

import ma.car.tishadow.bundle.update.util.TiAppUtil;

import org.appcelerator.titanium.TiProperties;

/**
 * @author wei.ding
 */
public class BundleUpdateManager implements Task {

	private final SeriesTask taskChain;

	public BundleUpdateManager() {
		super();

		this.taskChain = new SeriesTask();
		this.initial();
	}

	private void initial() {
		SeriesTask downloadAndDecompressBundleTask = new SeriesTask().addToQueue(new DownloadBundleTask()).addToQueue(new DecompressBundleTask());
		ParallelTask prepareToApplyBundleTask = new ParallelTask(downloadAndDecompressBundleTask, new BackupAppTask());

		this.taskChain.addToQueue(new CheckBundleTask()).addToQueue(prepareToApplyBundleTask).addToQueue(new ApplyBundleUpdateTask());
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(TaskContext context) {
		return this.taskChain.execute(context);
	}

	/**
	 * Checks if the latest bundle has been applied.
	 * @param context the task context by request
	 * @return true if applied, otherwise false
	 */
	public static boolean isLatestBundleApplied(TaskContext context) {
		TiProperties applicationProperties = context.getApplicationProperties();
		HashMap<String, ?> contextProperties = context.getContextProperties();

		boolean isUpdateReady = applicationProperties.getBool(TiAppUtil.UPDATE_READY_KEY, false);
		long currentBundleVersion = applicationProperties.getInt(TaskContext.Key.CURRENT_BUNDLE_VERSION, -1);
		Long latestBundleVersion = (Long) contextProperties.get(TaskContext.Key.LATEST_BUNDLE_VERSION);
		return isUpdateReady && latestBundleVersion != null && latestBundleVersion == currentBundleVersion;
	}

	/**
	 * Checks if the latest bundle need to be download.
	 * @param context task context by request.
	 * @return true if need, otherwise false.
	 */
	public static boolean isBundleDownloadRequired(TaskContext context) {
		String updateType = (String) context.getContextProperties().get(TaskContext.Key.UPDATE_TYPE);
		if ("dev_update".equalsIgnoreCase(updateType)) {
			return true;
		}
		if ("feature_toggle".equalsIgnoreCase(updateType) && !isLatestBundleApplied(context)) {
			return true;
		}
		return false;
	}

}
