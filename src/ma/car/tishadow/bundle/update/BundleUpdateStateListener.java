/**
 * 
 */
package ma.car.tishadow.bundle.update;


/**
 * @author wei.ding
 */
public interface BundleUpdateStateListener {

	/**
	 * Triggered when on bundle update state changed
	 * @param oldState old state
	 * @param newState new state
	 */
	public void onBundleUpdateStateChanged(BundleUpdateState oldState, BundleUpdateState newState, RequestProxy requestContext);
}
