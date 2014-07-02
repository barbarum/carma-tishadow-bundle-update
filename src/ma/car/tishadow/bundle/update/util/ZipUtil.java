/**
 * 
 */
package ma.car.tishadow.bundle.update.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;

/**
 * Represents an utility for basic compression/decompression in '.zip' extension. The implementation in this class is an extension of 'ti.compression' module,
 * and add extra ability to compress/de-compress asynchronized, will be modified later to expose proper behaviors.
 * @author wei.ding
 * @see {@link CompressionModule}
 */
public final class ZipUtil {

	private static final String SOURCE = "source";
	private static final String SRC = "src";
	private static final String DESTINATION = "destination";
	private static final String DEST = "dest";
	private static final String OVERWRITE = "overwrite";
	private static final ExecutorService POOL = Executors.newCachedThreadPool();

	/**
	 * Compresses a list of files into a simple zip file.
	 * @param dest the destination zip file.
	 * @param src the files which will be compressed.
	 * @return status message.
	 */
	public static String compress(String dest, Object[] src) {
		if (dest == null || dest.length() == 0) {
			return "archiveFile was not specified or was empty!";
		}
		TiBaseFile zip = TiFileFactory.createTitaniumFile(dest, false);

		if (src == null || src.length == 0) {
			return "fileArray was not specified or was empty!";
		}
		LinkedList<TiBaseFile> files = new LinkedList<TiBaseFile>();
		for (Object rawFile : src) {
			files.add(TiFileFactory.createTitaniumFile(rawFile.toString(), false));
		}

		// And then zip the files in to the archive.
		try {
			OutputStream fout = zip.getOutputStream();
			ZipOutputStream zout = new ZipOutputStream(new BufferedOutputStream(fout));

			while (!files.isEmpty()) {
				TiBaseFile file = files.remove();
				if (!file.exists()) {
					Log.e("ZipUtil", "Skipping over file, because it does not exist: " + file.nativePath());
				} else {
					ZipEntry ze = new ZipEntry(file.name());
					zout.putNextEntry(ze);
					writeInFile(file, zout);
					zout.closeEntry();
				}
			}
			zout.close();
			return "success";
		} catch (Exception e) {
			Log.e("ZipUtil", "Hit exception while unzipping the archive!", e);
			return e.toString();
		}
	}

	/**
	 * Compress a list of files into a simple zip file asynchronized.
	 */
	public static void compressAsyn(final HashMap arguments, final KrollFunction callback, final KrollObject context) {

		POOL.execute(new Runnable() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {

				String dest = (String) getValue(arguments, DEST, this.getValue(arguments, DESTINATION, null));
				String[] src = (String[]) getValue(arguments, SRC, this.getValue(arguments, SOURCE, null));

				String message = compress(dest, src);
				HashMap callbackArgs = new HashMap();
				callbackArgs.put("message", message);
				if (callback != null) {
					callback.callAsync(context, callbackArgs);
				}
			}

			private Object getValue(HashMap map, Object key, Object defaultValue) {
				return map.containsKey(key) ? map.get(key) : defaultValue;
			}
		});

	}

	/**
	 * Decompresses a zip file into another directory.
	 * @param dest the destination directory which will contanin all decompressed files.
	 * @param src the source zip file.
	 * @param overwrite, true if decompression can overwrite the same file into the destination directory, otherwise not.
	 * @return
	 */
	public static String decompress(String dest, String src, Boolean overwrite) {

		if (dest == null || dest.length() == 0) {
			return "destinationFolder was not specified or was empty!";
		}

		String destPath = TiFileFactory.createTitaniumFile(dest, false).getNativeFile().getAbsolutePath();

		if (src == null || src.length() == 0) {
			return "archiveFile was not specified or was empty!";
		}

		TiBaseFile zip = TiFileFactory.createTitaniumFile(src, false);

		if (!zip.exists()) {
			return "archiveFile was not found at " + src + "!";
		}

		if (overwrite == null) {
			return "overwrite was not specified!";
		}

		// And then unzip the archive.
		try {
			InputStream fin = zip.getInputStream();
			ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				String target = destPath + "/" + ze.getName();
				if (ze.isDirectory()) {
					ensureDirectoryExists(target);
				} else {
					File file = new File(target);
					if (overwrite || !file.exists()) {
						writeOutFile(file, target, zin);
					}
				}
			}
			zin.close();
			fin.close();
			return "success";
		} catch (Exception e) {
			Log.e("ZipUtil", "Hit exception while unzipping the archive!", e);
			return e.toString();
		}
	}

	public static void decompressAsyn(final HashMap arguments, final KrollFunction callback, final KrollObject context) {
		POOL.execute(new Runnable() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void run() {

				String dest = (String) getValue(arguments, DEST, this.getValue(arguments, DESTINATION, null));
				String src = (String) getValue(arguments, SRC, this.getValue(arguments, SOURCE, null));
				Boolean overwrite = (Boolean) getValue(arguments, OVERWRITE, null);

				String message = decompress(dest, src, overwrite);
				HashMap callbackArgs = new HashMap();
				callbackArgs.put("message", message);
				if (callback != null) {
					callback.callAsync(context, callbackArgs);
				}
			}

			private Object getValue(HashMap map, Object key, Object defaultValue) {
				return map.containsKey(key) ? map.get(key) : defaultValue;
			}

		});
	}

	public static void ensureDirectoryExists(String target) {
		File f = new File(target);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

	public static String unzip(Object[] args) {
		// Check that our arguments are valid.
		if (args.length != 3) {
			return "Invalid number of arguments provided!";
		}

		String dest = (String) args[0];
		String src = (String) args[1];
		Boolean overwrite = (Boolean) args[2];

		return decompress(dest, src, overwrite);
	}

	public static void unzipAsyn(HashMap arguments, KrollFunction callback, KrollObject context) {
		decompressAsyn(arguments, callback, context);
	}

	/**
	 * Compress a list of files into a single zip file.
	 * @param args
	 * @return
	 */
	public static String zip(Object[] args) {
		// Check that our arguments are valid.
		if (args.length != 2) {
			return "Invalid number of arguments provided!";
		}

		String dest = (String) args[0];
		Object[] src = (Object[]) args[1];

		return compress(dest, src);
	}

	public static void zipAsyn(HashMap arguments, KrollFunction callback, KrollObject context) {
		compressAsyn(arguments, callback, context);
	}

	private static void writeInFile(TiBaseFile tifile, ZipOutputStream zout) throws IOException {
		int size;
		byte[] buffer = new byte[2048];
		InputStream fos = tifile.getInputStream();
		BufferedInputStream bos = new BufferedInputStream(fos, buffer.length);
		while ((size = bos.read(buffer, 0, buffer.length)) != -1) {
			zout.write(buffer, 0, size);
		}
	}

	private static void writeOutFile(File file, String target, ZipInputStream zin) throws IOException {
		// Make sure the parent directory exists.
		file.getParentFile().mkdirs();
		// Write out the file.
		int size;
		byte[] buffer = new byte[2048];
		FileOutputStream fos = new FileOutputStream(target);
		BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
		while ((size = zin.read(buffer, 0, buffer.length)) != -1) {
			bos.write(buffer, 0, size);
		}
		bos.flush();
		bos.close();
	}
}
