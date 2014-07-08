/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;
import ma.car.tishadow.bundle.update.util.TiAppUtil;
import org.appcelerator.kroll.common.Log;

/**
 * @author wei.ding
 */
public class ParallelTask extends ArrayList<Task> implements Task {

	private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

	private static final AtomicInteger PARALLEL_TASK_IDENTIFIER = new AtomicInteger(1);

	private final String name;

	private final int identifier;

	public ParallelTask() {
		this((String) null);
	}

	/**
	 * Add a list of tasks into current task, which can be executed in parallel.
	 * @param tasks
	 */
	public ParallelTask(Task... tasks) {
		this((String) null, tasks);
	}

	/**
	 * Add a list of tasks into current task, which can be executed in parallel.
	 * @param tasks
	 */
	public ParallelTask(String name, Task... tasks) {
		this.name = name == null ? this.getClass().getName() : name;
		this.identifier = PARALLEL_TASK_IDENTIFIER.getAndIncrement();
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
			Log.i(getTag(), MessageFormat.format("No tasks in current parallel task ''{0}'', marked it as executed successfully.", this.getName()));
			return true;
		}
		Log.i(getTag(), "Starting parallel task '" + this.getName() + "'...");
		final CountDownLatch latch = new CountDownLatch(this.size());
		final AtomicBoolean expectedResult = new AtomicBoolean(true);
		for (final Task task : this) {
			executeAsyn(context, latch, expectedResult, task);
		}
		try {
			latch.await();
			if (expectedResult.get()) {
				Log.i(getTag(), "Parallel task '" + this.getName() + "' executed successfully.");
			}
			return expectedResult.get();
		} catch (InterruptedException e) {
			Log.e(getTag(), "Task '" + this.getName() + "' has been interrupted.");
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
						String message = "ParallelTask ''{0}'' executed completely because fail to execute ''{1}''.";
						Log.i(getTag(), MessageFormat.format(message, ParallelTask.this.getName(), task.getClass().getName()));
					} else {
						Log.i(getTag(), "The result of ParallelTask '" + ParallelTask.this.getName() + "' has been set to false.");
					}
				}
				Log.i(getTag(), "Task '" + task.getClass().getName() + "' executed successfully.");
				latch.countDown();
			}

		});
	}

	private String getTag() {
		return this.getClass().getSimpleName() + "-" + this.identifier;
	}

	/**
	 * @return the name
	 */
	private String getName() {
		return name;
	}
}
