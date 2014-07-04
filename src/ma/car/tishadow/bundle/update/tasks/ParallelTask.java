/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import android.util.Log;
import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;
import ma.car.tishadow.bundle.update.util.TiAppUtil;

/**
 * @author wei.ding
 */
public class ParallelTask extends ArrayList<Task> implements Task {

	private static final String TAG = "ParallelTask";

	private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

	public ParallelTask() {
		super();
	}

	public ParallelTask(Collection<? extends Task> c) {
		super(c);
	}

	/**
	 * Add a list of tasks into current task, which can be executed in parallel.
	 * @param tasks
	 */
	public ParallelTask(Task... tasks) {
		this();
		for (Task task : tasks) {
			this.add(task);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute()
	 */
	@Override
	public boolean execute(final RequestProxy context) {
		if (this.size() == 0) {
			return true;
		}
		final CountDownLatch latch = new CountDownLatch(this.size());
		final AtomicBoolean expectedResult = new AtomicBoolean(true);
		for (final Task task : this) {
			executeAsyn(context, latch, expectedResult, task);
		}
		try {
			latch.await();
			return expectedResult.get();
		} catch (InterruptedException e) {
			Log.e(TAG, "ParallelTask '" + this + "' has been interrupted.");
		}
		return false;
	}

	private void executeAsyn(final RequestProxy context, final CountDownLatch latch, final AtomicBoolean expectedResult, final Task task) {
		TiAppUtil.THREAD_POOL.execute(new Runnable() {

			@Override
			public void run() {
				boolean result = task.execute(context);
				if (!result) {
					if (expectedResult.compareAndSet(true, false)) {
						Log.i(TAG, "ParallelTask '" + ParallelTask.this + "' failed because fail to execute " + task);
					} else {
						Log.i(TAG, "The result of ParallelTask '" + ParallelTask.this + "' has been set to false.");
					}
				}
				latch.countDown();
			}

		});
	}

}
