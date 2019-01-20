package com.pisen.router.core.filemanager.transfer;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import android.net.Uri;
import android.os.SystemClock;

public class TransferUtils {

	public static Random sRandom = new Random(SystemClock.uptimeMillis());
	public static final String FILENAME_SEQUENCE_SEPARATOR = "-";
	private static final Object sUniqueLock = new Object();

	public static String generateSaveFile(String url) throws IOException {
		final File file = new File(Uri.parse(url).getPath());
		File parent = file.getParentFile().getAbsoluteFile();
		String name = file.getName();

		// Ensure target directories are ready
		if (!(parent.isDirectory() || parent.mkdirs())) {
			throw new IOException("Failed to create parent for " + parent);
		}

		final String prefix;
		final String suffix;
		final int dotIndex = name.lastIndexOf('.');
		if (dotIndex < 0) {
			prefix = name;
			suffix = "";
		} else {
			prefix = name.substring(0, dotIndex);
			suffix = name.substring(dotIndex);
		}

		synchronized (sUniqueLock) {
			String newName = generateAvailableFilenameLocked(parent, prefix, suffix);
			final File newFile = new File(parent, newName);
			newFile.createNewFile();
			return newFile.getAbsolutePath();
		}
	}

	private static String generateAvailableFilenameLocked(File parent, String prefix, String suffix) throws IOException {
		String name = prefix + suffix;
		if (isFilenameAvailableLocked(parent, name)) {
			return name;
		}

		int sequence = 1;
		for (int magnitude = 1; magnitude < 1000000000; magnitude *= 10) {
			for (int iteration = 0; iteration < 9; ++iteration) {
				name = prefix + TransferUtils.FILENAME_SEQUENCE_SEPARATOR + sequence + suffix;
				if (isFilenameAvailableLocked(parent, name)) {
					return name;
				}
				sequence += sRandom.nextInt(magnitude) + 1;
			}
		}

		throw new IOException("Failed to generate an available filename");
	}

	private static boolean isFilenameAvailableLocked(File parent, String name) {
		if (new File(parent, name).exists()) {
			return false;
		}

		return true;
	}

}
