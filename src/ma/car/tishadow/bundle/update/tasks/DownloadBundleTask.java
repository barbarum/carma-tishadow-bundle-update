/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.appcelerator.kroll.common.Log;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

/**
 * @author wei.ding
 */
public class DownloadBundleTask implements Task {

	private static final String TAG = "DownloadBundleTask";

	private final Lock lock = new ReentrantLock();
	private final Condition waitForDownloadComplete = lock.newCondition();

	/*
	 * (non-Javadoc)
	 * @see ma.car.tishadow.bundle.update.tasks.Task#execute(ma.car.tishadow.bundle.update.tasks.TaskContext)
	 */
	@Override
	public boolean execute(TaskContext context) {
		if (!BundleUpdateManager.isBundleDownloadRequired(context)) {
			return true;
		}
		lock.lock();
		try {
			sendBundleDownloadRequest(context);
			waitForDownloadComplete.await();
			return true;
		} catch (InterruptedException e) {
			Log.e(TAG, "Failed to handle post tasks if the bundle would be download later because of bundle update thread is interrupted.", e);
		} finally {
			lock.unlock();
		}

		return false;
	}

	private void sendBundleDownloadRequest(TaskContext context) {

		// Register ACTION_DOWNLOAD_COMPLETE event, and send download request to Android DownloadManager service.
		Context applicationContext = context.getApplicationContext();
		applicationContext.registerReceiver(newDownloadCompleteHandler(context), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		DownloadManager downloadManager = (DownloadManager) applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(""));
		request.setShowRunningNotification(Log.isDebugModeEnabled()).setVisibleInDownloadsUi(Log.isDebugModeEnabled());

		long downloadId = downloadManager.enqueue(request);

		context.getContextProperties().put(TaskContext.Key.DOWNLOADING_BUNDLE_REFID, new Long(downloadId));
		context.markedBundleUpdateProcessTo(BundleUpdateProcess.DOWNLOADING);
	}

	/**
	 * Create a new download complete handler, which is an implementation of broadcastReceiver to receive notification event from android system.
	 * @param context
	 * @return
	 */
	private BroadcastReceiver newDownloadCompleteHandler(final TaskContext context) {
		return new BroadcastReceiver() {

			// TODO The android default DownloadManager may not be stable because of an open bug <a
			// href='https://code.google.com/p/android/issues/detail?id=18462' target='_blank'>issue 18462</a>, I may need to rewrite this component later.
			@Override
			public void onReceive(Context cxt, Intent intent) {
				lock.lock();
				try {
					long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
					long storedReferenceId = (Long) context.getContextProperties().get(TaskContext.Key.DOWNLOADING_BUNDLE_REFID);
					if (referenceId != -1 && referenceId == storedReferenceId) {
						onBundleDownloadCompleted(context, intent);
						waitForDownloadComplete.signal();
					}
				} finally {
					lock.unlock();
				}
			}

		};
	}

	private void onBundleDownloadCompleted(TaskContext context, Intent intent) {
		long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

		Query query = new Query();
		query.setFilterById(referenceId);

		Context applicationContext = context.getApplicationContext();
		DownloadManager downloadManager = (DownloadManager) applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);

		Cursor cursor = downloadManager.query(query);

		if (cursor.moveToFirst()) {
			int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
			int status = cursor.getInt(statusIndex);
			int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
			int reason = reasonIndex == -1 ? -1 : cursor.getInt(reasonIndex);
			Log.i(TAG, "Bundle download completed, status(" + status + "), reason(" + reason + ").");
		}

		cursor.close();
	}

}
