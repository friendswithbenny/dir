package org.fwb.file;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class FileUtil {
	/** @deprecated static utilities only */
	@Deprecated
	private FileUtil() { }
	
	/**
	 * returns the extension (after the last dot) not-including the dot, or null if there is no dot.
	 * n.b. the guava alternative without nulls ({@link com.google.common.io.Files.getFileExtension})
	 */
	public static String getExtension(String s) {
		int i = s.lastIndexOf('.');
		return 0 > i ? null : s.substring(i + 1);
	}
	
	/**
	 * returns the filename without extension (nor dot) or the full name if there is no dot.
	 * n.b. the guava alternative without nulls ({@link com.google.common.io.Files.getFileNameWithoutExtension})
	 */
	public static String getSimpleName(String s) {
		int i = s.lastIndexOf('.');
		return 0 > i ? s : s.substring(0, i);
	}
	
	static final String
		PATTERN_SECONDS = "yyyy-MM-dd_HHmm-ss",
		PATTERN_TIMESTAMP = "yyyy-MM-dd_HHmm-ss.SSSZ";
	public static DateFormat seconds() {
		return new SimpleDateFormat(PATTERN_SECONDS);
	}
	public static DateFormat timestamp() {
		return new SimpleDateFormat(PATTERN_TIMESTAMP);
	}
	
	public static String getSizeString(long size) {
		NumberFormat fmt = new ByteSizeFormat();
		return fmt.format(size);
	}

}
