/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package ma.car.tishadow.bundle.update;

import java.util.concurrent.LinkedBlockingQueue;

import ma.car.tishadow.bundle.update.tasks.BundleUpdateManager;
import ma.car.tishadow.bundle.update.util.TiAppUtil;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

@Kroll.module(name = "TishadowBundleUpdate", id = "ma.car.tishadow.bundle.update")
public class TishadowBundleUpdateModule extends KrollModule {

	private static final String TAG = "TishadowBundleUpdateModule";

	public static final long BACKWARD_COMPATIBLE_UID = 1L;

	private LinkedBlockingQueue<RequestProxy> queue;

	public TishadowBundleUpdateModule() {
		super();
		queue = new LinkedBlockingQueue<RequestProxy>();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {

		// Do some stuff when the app is created.
		Log.i(TAG, "Tishadow Bundule Update Module loaded.");

	}

	// Public methods
	@Kroll.method
	public void send(final RequestProxy request) {
		request.setJavascriptContext(getKrollObject());
		queue.add(request);
		handleRequest();
	}

	private void handleRequest() {
		final RequestProxy request = queue.poll();
		if (request == null) {
			return;
		}
		TiAppUtil.THREAD_POOL.execute(new Runnable() {

			@Override
			public void run() {
				new BundleUpdateManager().execute(request);
				handleRequest();
			}

		});
	}

	@Kroll.method
	public void doBundleUpdate() {

		RequestProxy context = new RequestProxy(getActivity());
		context.setJavascriptContext(getKrollObject());

		new BundleUpdateManager().execute(context);
	}

}
