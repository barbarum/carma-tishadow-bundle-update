/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

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
}
