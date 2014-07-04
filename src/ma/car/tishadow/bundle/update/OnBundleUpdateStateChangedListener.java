/**
 * 
 */
package ma.car.tishadow.bundle.update;

import ma.car.tishadow.bundle.update.tasks.BundleUpdateState;

/**
 * @author wei.ding
 */
public interface OnBundleUpdateStateChangedListener {

	/**
	 * Triggered when on bundle update state changed
	 * @param oldState old state
	 * @param newState new state
	 */
	public void onBundleUpdateStateChanged(BundleUpdateState oldState, BundleUpdateState newState, RequestProxy requestContext);
}
