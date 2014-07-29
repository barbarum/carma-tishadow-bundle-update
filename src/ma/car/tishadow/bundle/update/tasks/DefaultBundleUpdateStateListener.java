/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.HashMap;

import ma.car.tishadow.bundle.update.BundleUpdateState;
import ma.car.tishadow.bundle.update.BundleUpdateStateListener;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.util.ManifestUtil;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.common.Log;

/**
 * @author wei.ding
 */
public class DefaultBundleUpdateStateListener implements BundleUpdateStateListener {

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.OnBundleUpdateStateChangedListener#onBundleUpdateStateChanged(ma.car.tishadow.bundle.update.tasks.BundleUpdateState,
	 * ma.car.tishadow.bundle.update.tasks.BundleUpdateState)
	 */
	@Override
	public void onBundleUpdateStateChanged(BundleUpdateState oldState, BundleUpdateState newState, RequestProxy requestContext) {
		switch (newState) {
			case APPLIED:
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
				if (requestContext.getForceUpdateRequiredListener() != null && isForceUpdateRequired(requestContext)) {
					requestContext.getForceUpdateRequiredListener().onForceUpdateRequired();
				}
				callAsync(requestContext, newState, RequestProxy.Key.ON_BUNDLE_READY_FOR_APPLY_CALLBACK);
				break;
			case CHECKED:
				requestContext.clearBundleUpdateInfo();
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
		if (newState == BundleUpdateState.READY_FOR_APPLY) {
			arguments.put("forceUpdate", isForceUpdateRequired(requestContext));
		}
		onStateChanged.callAsync(requestContext.getKrollObject(), arguments);
	}

	private boolean isForceUpdateRequired(RequestProxy requestContext) {
		try {
			return ManifestUtil.isBundleRequireForceUpdate(ManifestUtil.getBundleManifest(requestContext.getBackupDirectory()));
		} catch (Exception e) {
			Log.e("BundleStateListener", "Failed to read manifest file from standby directory.", e);
		}
		return false;
	}

}
