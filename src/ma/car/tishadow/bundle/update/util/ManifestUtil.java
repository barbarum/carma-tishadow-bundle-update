/**
 * 
 */
package ma.car.tishadow.bundle.update.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an utility to manipulate bundle manifest.
 * @author wei.ding
 */
public final class ManifestUtil {

	public static class Patch {

		private List<String> filesToDelete;
		private List<String> filesToAdd;
		private List<String> filesToUpdate;

		public Patch() {
			super();
			this.filesToAdd = new ArrayList<String>();
			this.filesToUpdate = new ArrayList<String>();
			this.filesToDelete = new ArrayList<String>();
		}

		public List<String> getFilesToAdd() {
			return filesToAdd;
		}

		public List<String> getFilesToDelete() {
			return filesToDelete;
		}

		public List<String> getFilesToUpdate() {
			return filesToUpdate;
		}

		public boolean isEmpty() {
			return this.filesToAdd.isEmpty() && this.filesToUpdate.isEmpty() && this.filesToDelete.isEmpty();
		}
	}

	private static final String COLUMN_SYM = ",";
	private static final String COMMENT_SYM = "#";
	private static final String BUNDLE_VERSION_REGEXP = "[^\\d]";
	private static final String BUNDLE_VERSION_HEAD_PATTERN = "^" + COMMENT_SYM + "Created:[\\d]+$";
	private static final String FORCE_UPDATE_SIGN_HEAD_PATTERN = COMMENT_SYM + "ForceUpdate:";

	/**
	 * Compare two manifests, and return the differences.
	 * @param oldManifest
	 * @param newManifest
	 * @return
	 * @throws IOException
	 */
	public static Patch compareManifest(File oldManifest, File newManifest) throws IOException {

		Map<String, String> oldManifestMap = translate(oldManifest);
		Map<String, String> newManifestMap = translate(newManifest);

		Patch patch = new Patch();
		for (Map.Entry<String, String> entry : newManifestMap.entrySet()) {
			if (!oldManifestMap.containsKey(entry)) {
				patch.getFilesToAdd().add(entry.getKey());
			} else {
				if (entry.getValue() != oldManifestMap.get(entry.getKey())) {
					patch.getFilesToUpdate().add(entry.getKey());
				}
				oldManifestMap.remove(entry.getKey());
			}
		}
		for (Map.Entry<String, String> entry : oldManifestMap.entrySet()) {
			patch.getFilesToDelete().add(entry.getKey());
		}
		return patch;
	}

	/**
	 * Reads the bundle version from manifest.
	 * @param manifest
	 * @return
	 * @throws IOException
	 * @throws ManifestParseException
	 */
	public static long readBundleVersion(File manifest) throws IOException, ManifestParseException {
		if (manifest == null || !manifest.exists()) {
			return -1L;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(manifest));
			String line = reader.readLine();
			if (line == null || !line.matches(BUNDLE_VERSION_HEAD_PATTERN)) {
				throw new ManifestParseException();
			}
			return Long.parseLong(line.replaceAll(BUNDLE_VERSION_REGEXP, ""));
		} finally {
			closeInputStream(reader);
		}
	}

	/**
	 * Reads the bundle version from manifest.
	 * @param manifest
	 * @return
	 * @throws IOException
	 * @throws ManifestParseException
	 */
	public static boolean readBundleUpdateType(File manifest) throws IOException, ManifestParseException {
		if (manifest == null || !manifest.exists()) {
			return false;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(manifest));
			reader.readLine();
			String line = reader.readLine();
			if (line == null || !line.startsWith(FORCE_UPDATE_SIGN_HEAD_PATTERN)) {
				throw new ManifestParseException();
			}
			return "true".equalsIgnoreCase(line.replaceAll(FORCE_UPDATE_SIGN_HEAD_PATTERN, ""));
		} finally {
			closeInputStream(reader);
		}
	}

	private static void closeInputStream(BufferedReader reader) {
		if (reader == null) {
			return;
		}
		try {
			reader.close();
		} catch (IOException e) {

		}
	}

	private static Map<String, String> translate(File manifest) throws IOException {
		if (manifest == null || !manifest.exists()) {
			return Collections.<String, String> emptyMap();
		}
		Map<String, String> map = new HashMap<String, String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(manifest));
			String line;
			String[] columns;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(COMMENT_SYM)) {
					continue;
				}
				columns = line.split(COLUMN_SYM, -1);
				if (columns.length >= 2) {
					map.put(columns[0], columns[1]);
				}
			}
		} finally {
			closeInputStream(reader);
		}
		return map;
	}

}
