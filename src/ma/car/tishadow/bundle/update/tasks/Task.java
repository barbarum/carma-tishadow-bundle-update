/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import ma.car.tishadow.bundle.update.RequestProxy;

/**
 * Represents a single task, which can be one of the following 3 tasks:
 * <ul>
 * <li>single task to complete a specific task</li>
 * <li>series task to executes a series of tasks by the order FIFO, and marked it as completed until last one has finished.</li>
 * <li>parallel task to executes a series of tasks in parallel, and marked it as completed until all of them has finished.</li>
 * </ul>
 * @author wei.ding
 */
public interface Task {

	/**
	 * Executes current task.
	 */
	public boolean execute(RequestProxy context);
}
