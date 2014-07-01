/**
 * 
 */
package ma.car.tishadow.bundle.update.tasks;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import ma.car.tishadow.bundle.update.ApplicationState;

import org.appcelerator.kroll.KrollObject;

import android.content.Context;

/**
 * @author wei.ding
 */
public class TaskContext {

	private Properties properties;

	private AtomicReference<ApplicationState> stateRef = new AtomicReference<ApplicationState>(ApplicationState.UNKNOWN);

	private KrollObject javascriptContext;

	private Context applicationContext;

	public TaskContext() {
		this(new Properties());
	}

	/**
	 * @param properties
	 */
	public TaskContext(Properties properties) {
		super();
		this.properties = properties;
	}

	/**
	 * Gets long value by key.
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public long getLongValue(String key, long defaultValue) {

		if (this.properties.containsKey(key)) {
			return Long.parseLong(this.properties.getProperty(key, "" + defaultValue));
		}

		return defaultValue;
	}

	public String getStringValue(String key, String defaultValue) {
		return this.properties.getProperty(key, defaultValue);
	}

	/**
	 * Changes the application's bundle state.
	 * @param state
	 */
	public void setApplicationState(ApplicationState state) {
		this.stateRef.set(state);
	}

	/**
	 * @return the javascriptContext
	 */
	public KrollObject getJavascriptContext() {
		return javascriptContext;
	}

	/**
	 * @param javascriptContext the javascriptContext to set
	 */
	public void setJavascriptContext(KrollObject javascriptContext) {
		this.javascriptContext = javascriptContext;
	}

	/**
	 * @return the applicationContext
	 */
	public Context getApplicationContext() {
		return applicationContext;
	}

	/**
	 * @param applicationContext the applicationContext to set
	 */
	public void setApplicationContext(Context applicationContext) {
		this.applicationContext = applicationContext;
	}

	public static class Key {

		public static final String BACKUP_DIRECTORY = "standby_dir";

	}
}
