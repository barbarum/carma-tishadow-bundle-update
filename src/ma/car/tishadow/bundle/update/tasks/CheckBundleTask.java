/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

/**
 * @author wei.ding
 */
public class CheckBundleTask implements Task {

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(RequestContext context) {
		String updateType = context.getApplicationProperties().getString(RequestContext.Key.UPDATE_TYPE, null);
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
	
	
}
