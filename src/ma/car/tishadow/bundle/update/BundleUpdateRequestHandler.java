/**
 * 
 */
package ma.car.tishadow.bundle.update;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import ma.car.tishadow.bundle.update.tasks.ApplyBundleUpdateOnlineTask;
import ma.car.tishadow.bundle.update.tasks.BundleUpdateManager;
import ma.car.tishadow.bundle.update.tasks.DefaultBundleUpdateStateListener;
import ma.car.tishadow.bundle.update.tasks.Task;
import ma.car.tishadow.bundle.update.util.TiAppUtil;
import android.util.Log;

/**
 * Represents a singleton handler to handle all bundle update request in the FIFO queue.
 * @author wei.ding
 */
public class BundleUpdateRequestHandler {

	private AtomicBoolean handling = new AtomicBoolean(false);

	private final LinkedBlockingQueue<RequestTaskEntry> queue;
	private final BundleUpdateStateListener stateChangedListener;

	private static class Singleton {

		public static final BundleUpdateRequestHandler INSTANCE = new BundleUpdateRequestHandler();
	}

	private BundleUpdateRequestHandler() {
		super();
		this.queue = new LinkedBlockingQueue<RequestTaskEntry>();
		this.stateChangedListener = new DefaultBundleUpdateStateListener();
	}

	/**
	 * Gets singleton handler instance.
	 * @return
	 */
	public static BundleUpdateRequestHandler getInstance() {
		return Singleton.INSTANCE;
	}

	/**
	 * Sends a bundle update request into queue.
	 * @param request
	 */
	public void sendRequest(RequestProxy request) {
		if (request == null) {
			throw new NullPointerException("request can not be null.");
		}
		this.queue.add(new RequestTaskEntry(request, new BundleUpdateManager()));
		handleRequest();
	}

	/**
	 * Applies pending standby resources online.
	 * @param request
	 */
	public void applyPendingUpdateOnline(final RequestProxy request) {
		TiAppUtil.THREAD_POOL.execute(new Runnable() {

			@Override
			public void run() {
				request.setOnBundleUpdateStateChangedListener(new DefaultBundleUpdateStateListener());
				new ApplyBundleUpdateOnlineTask().execute(request);
				request.setOnBundleUpdateStateChangedListener(null);
			}

		});
	}

	private void handleRequest() {
		if (!handling.compareAndSet(false, true)) {
			Log.i("BundleUpdateRequestHandler", "There is a request in progress now, will wait for current request be handled.");
			return;
		}
		final RequestTaskEntry entry = this.queue.poll();
		if (entry == null) {
			Log.i("BundleUpdateRequestHandler", "Empty queue, will exit current round process.");
			return;
		}
		TiAppUtil.THREAD_POOL.execute(new Runnable() {

			@Override
			public void run() {
				entry.setOnBundleUpdateStateChangedListener(stateChangedListener);
				entry.execute();
				entry.setOnBundleUpdateStateChangedListener(null);
				handling.set(false);
				handleRequest();
			}

		});

	}

	private static class RequestTaskEntry {

		private RequestProxy request;
		private Task task;

		/**
		 * @param request
		 * @param task
		 */
		public RequestTaskEntry(RequestProxy request, Task task) {
			super();
			this.request = request;
			this.task = task;
		}

		/**
		 * @return the request
		 */
		public RequestProxy getRequest() {
			return request;
		}

		/**
		 * @return the task
		 */
		public Task getTask() {
			return task;
		}

		/**
		 * @param context
		 * @return
		 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.RequestProxy)
		 */
		public boolean execute() {
			return this.getTask().execute(getRequest());
		}

		/**
		 * @param onBundleUpdateStateChangedListener
		 * @see ma.car.tishadow.bundle.update.RequestProxy#setOnBundleUpdateStateChangedListener(ma.car.tishadow.bundle.update.BundleUpdateStateListener)
		 */
		public void setOnBundleUpdateStateChangedListener(BundleUpdateStateListener onBundleUpdateStateChangedListener) {
			request.setOnBundleUpdateStateChangedListener(onBundleUpdateStateChangedListener);
		}

	}
}
