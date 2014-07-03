/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.LinkedList;

import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;

/**
 * @author wei.ding
 */
public class SeriesTask extends LinkedList<Task> implements Task {

	private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

	public SeriesTask() {
		super();
	}

	/**
	 * Create a new series task, and add initialTask into head of this task chain.
	 * @param initialTask first task of current series of tasks.
	 */
	public SeriesTask(Task initialTask) {
		super();
		this.addToQueue(initialTask);
	}

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute()
	 */
	@Override
	public boolean execute(RequestContext context) {

		return false;
	}

	public SeriesTask addToQueue(Task task) {
		this.add(task);
		return this;
	}
}
