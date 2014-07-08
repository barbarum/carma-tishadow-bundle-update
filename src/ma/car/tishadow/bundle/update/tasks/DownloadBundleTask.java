/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.io.File;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ma.car.tishadow.bundle.update.RequestProxy;

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
 * Represents a task to download the specific bundle.
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
	public boolean execute(RequestProxy context) {
		Log.v(TAG, "Starting DownloadBundleTask...");
		if (!BundleUpdateManager.isBundleDownloadRequired(context)) {
			Log.i(TAG, "Don't need to download bundle, because already up-to-date.");
			return true;
		}
		lock.lock();
		try {
			Log.d(TAG, "starting to downloading bundle...");
			sendBundleDownloadRequest(context);
			waitForDownloadComplete.await();
			context.markedBundleUpdateStateTo(BundleUpdateState.DOWNLOADED);
			Log.d(TAG, "DownloadBundleTask done.");
			return true;
		} catch (InterruptedException e) {
			context.markedBundleUpdateStateTo(BundleUpdateState.INTERRUPTED);
			Log.e(TAG, "Failed to handle post tasks if the bundle would be download later because of bundle update thread is interrupted.", e);
		} finally {
			lock.unlock();
		}

		return false;
	}

	private void sendBundleDownloadRequest(RequestProxy context) {

		// Register ACTION_DOWNLOAD_COMPLETE event, and send download request to Android DownloadManager service.
		Context applicationContext = context.getApplicationContext();

		applicationContext.registerReceiver(newDownloadCompleteHandler(context), new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		DownloadManager downloadManager = (DownloadManager) applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
		String bundleDownloadUrl = (String) context.getRequestProperty(RequestProxy.Key.BUNDLE_DOWNLOAD_URL);
		Object latestBundleVersion = context.getRequestProperty(RequestProxy.Key.LATEST_BUNDLE_VERSION);
		String bundleDecompressDirectory = (String) context.getRequestProperty(RequestProxy.Key.BUNDLE_DECOMPRESS_DIRECTORY);
		String bundleLocalFileName = bundleDecompressDirectory + ".zip";
		File destination = new File(context.getExternalApplicationTemporaryDirectory(), bundleLocalFileName);

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(bundleDownloadUrl));
		request.setShowRunningNotification(Log.isDebugModeEnabled()).setVisibleInDownloadsUi(Log.isDebugModeEnabled());
		request.setDestinationUri(Uri.fromFile(destination));
		long downloadId = downloadManager.enqueue(request);

		context.getRequestProperties().put(RequestProxy.Key.DOWNLOADING_BUNDLE_REFID, new Long(downloadId));
		context.getRequestProperties().put(RequestProxy.Key.DOWNLOAD_DESTINATION_FILENAME, bundleLocalFileName);
		context.markedBundleUpdateStateTo(BundleUpdateState.DOWNLOADING);

		Log.i(TAG, "Downloading bundle '" + latestBundleVersion + "' from '" + bundleDownloadUrl + "' into directory '" + destination + "'... ");
	}

	/**
	 * Create a new download complete handler, which is an implementation of broadcastReceiver to receive notification event from android system.
	 * @param context
	 * @return
	 */
	private BroadcastReceiver newDownloadCompleteHandler(final RequestProxy context) {
		return new BroadcastReceiver() {

			// TODO The android default DownloadManager may not be stable because of an open bug <a
			// href='https://code.google.com/p/android/issues/detail?id=18462' target='_blank'>issue 18462</a>, I may need to rewrite this component later.
			@Override
			public void onReceive(Context cxt, Intent intent) {
				Log.i(TAG, "Receiving download complete event intent '" + intent + "'...");
				lock.lock();
				try {
					long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L);
					long storedReferenceId = (Long) context.getRequestProperties().get(RequestProxy.Key.DOWNLOADING_BUNDLE_REFID);
					if (referenceId != -1 && referenceId == storedReferenceId) {
						onBundleDownloadCompleted(context, intent);
						context.getRequestProperties().remove(RequestProxy.Key.DOWNLOADING_BUNDLE_REFID);
						cxt.unregisterReceiver(this);
						waitForDownloadComplete.signal();

					}
				} finally {
					lock.unlock();
				}
			}

		};
	}

	private void onBundleDownloadCompleted(RequestProxy context, Intent intent) {
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

			int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
			String localUri = cursor.getString(localUriIndex);

			Log.i(TAG, "Bundle download completed, status(" + status + "), reason(" + reason + "), local uri(" + localUri + ").");
		}

		cursor.close();
	}

}
