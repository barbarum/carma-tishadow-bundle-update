/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import ma.car.tishadow.bundle.update.RequestProxy;
import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;
import org.appcelerator.kroll.common.Log;

/**
 * @author wei.ding
 */
public class SeriesTask extends LinkedList<Task> implements Task {

	private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

	private static final AtomicInteger SERIES_TASK_IDENTIFIER = new AtomicInteger(1);

	private final String name;

	private final int identifier;

	/**
	 * 
	 */
	public SeriesTask() {
		this(null, null);
	}

	/**
	 * @param name
	 */
	public SeriesTask(String name) {
		this(name, null);
	}

	/**
	 * Create a new series task, and add initialTask into head of this task chain.
	 * @param initialTask first task of current series of tasks.
	 */
	public SeriesTask(Task initialTask) {
		this(null, initialTask);
	}

	/**
	 * @param name
	 */
	public SeriesTask(String name, Task initialTask) {
		this.name = name == null ? this.getClass().getName() : name;
		this.identifier = SERIES_TASK_IDENTIFIER.getAndIncrement();
		if (initialTask != null) {
			this.addToQueue(initialTask);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute()
	 */
	@Override
	public boolean execute(RequestProxy context) {
		if (this.size() == 0) {
			Log.i(getTag(), MessageFormat.format("No tasks in current series task ''{0}'', marked it as executed successfully.", this.getName()));
			return true;
		}
		Log.i(getTag(), MessageFormat.format("Starting series task ''{0}''...", this.getName()));
		for (Task task : this) {
			if (!task.execute(context)) {
				String message = "Task ''{0}'' executed completely, but return flag false to notify to finish current series task ''{1}''.";
				Log.i(getTag(), MessageFormat.format(message, task.getClass(), this.getName()));
				return false;
			}
			Log.v(getTag(), MessageFormat.format("Task ''{0}'' executed successfully, looking for next task...", task.getClass().getName()));
		}
		Log.i(getTag(), MessageFormat.format("Series task ''{0}'' executed successfully.", this.getName()));
		return true;
	}

	public SeriesTask addToQueue(Task task) {
		this.add(task);
		return this;
	}

	/**
	 * @return the name
	 */
	private String getName() {
		return name;
	}

	private String getTag() {
		return this.getClass().getSimpleName() + "-" + this.identifier;
	}
}
