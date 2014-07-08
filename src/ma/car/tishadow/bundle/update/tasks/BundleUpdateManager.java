/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.HashMap;

import ma.car.tishadow.bundle.update.OnBundleUpdateStateChangedListener;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;
import ma.car.tishadow.bundle.update.util.TiAppUtil.PropertyKey;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.titanium.TiProperties;

import org.appcelerator.kroll.common.Log;

/**
 * TiShadow Bundle Update Manager
 * @author wei.ding
 */
public class BundleUpdateManager implements Task, OnBundleUpdateStateChangedListener {

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
				context.markedBundleUpdateStateTo(result ? BundleUpdateState.READY_FOR_APPLY : BundleUpdateState.INTERRUPTED);
				return result;
			}

		};

		this.taskChain.addToQueue(new CheckBundleTask()).addToQueue(prepareToApplyBundleTask).addToQueue(new ApplyPatchTask())
				.addToQueue(new ApplyUpdateTask());
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestProxy context) {
		Log.i("BundleUpdateManager", "Start executing bundle update tasks...");
		context.setOnBundleUpdateStateChangedListener(this);
		boolean result = this.taskChain.execute(context);
		context.setOnBundleUpdateStateChangedListener(null);
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
		Integer latestBundleVersion = (Integer) context.getRequestProperty(RequestProxy.Key.LATEST_BUNDLE_VERSION);
		return latestBundleVersion != null && latestBundleVersion <= currentBundleVersion;
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
		if ("feature_toggle".equalsIgnoreCase(updateType) && !isLatestBundleApplied(context)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.OnBundleUpdateStateChangedListener#onBundleUpdateStateChanged(ma.car.tishadow.bundle.update.tasks.BundleUpdateState,
	 * ma.car.tishadow.bundle.update.tasks.BundleUpdateState)
	 */
	@Override
	public void onBundleUpdateStateChanged(BundleUpdateState oldState, BundleUpdateState newState, RequestProxy requestContext) {
		switch (newState) {
			case APPLYED:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_APPLIED_CALLBACK);
				break;
			case APPLYING:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_APPLYING_CALLBACK);
				break;
			case DECOMPRESSED:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_EXTRACTED_CALLBACK);
				break;
			case DOWNLOADED:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_DOWNLOAD_CALLBACK);
				break;
			case DOWNLOADING:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_DOWNLOADING_CALLBACK);
				break;
			case READY_FOR_APPLY:
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_READY_FOR_APPLY_CALLBACK);
				break;
			case CHECKED:
				break;
			case STARTING:
				break;
			default:
				break;
		}

		callAsync(requestContext, newState, RequestProxy.Key.ON_STATE_CHANGED_CALLBACK);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void callAsync(RequestProxy requestContext, BundleUpdateState newState, String key) {
		KrollFunction onStateChanged = requestContext.getRequestCallback(key);
		if (onStateChanged == null) {
			return;
		}
		HashMap arguments = new HashMap();
		arguments.put("state", newState.toString());
		onStateChanged.callAsync(requestContext.getKrollObject(), arguments);
	}

}
