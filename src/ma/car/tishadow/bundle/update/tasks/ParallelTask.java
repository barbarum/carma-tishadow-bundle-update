/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.ArrayList;
import java.util.Collection;

import ma.car.tishadow.bundle.update.TishadowBundleUpdateModule;

/**
 * @author wei.ding
 */
public class ParallelTask extends ArrayList<Task> implements Task {

	private static final long serialVersionUID = TishadowBundleUpdateModule.BACKWARD_COMPATIBLE_UID;

	/**
	 * 
	 */
	public ParallelTask() {
		super();
	}

	/**
	 * @param c
	 */
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
	public boolean execute(TaskContext context) {

		return false;
	}

}
