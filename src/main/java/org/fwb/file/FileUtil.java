package org.fwb.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.fwb.io.StreamUtil;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;

public class FileUtil {
	/** @deprecated static utilities only */
	@Deprecated
	private FileUtil() { }
	
	/**
	 * returns the extension (after the last dot) not-including the dot, or null if there is no dot.
	 * n.b. the guava alternative without nulls ({@link com.google.common.io.Files.getFileExtension})
	 */
	public static String getExtension(String s) {
		int i = findExtension(s);
		return 0 > i ? null : s.substring(i + 1);
	}
	
	/**
	 * returns the filename without extension (nor dot) or the full name if there is no dot.
	 * n.b. the guava alternative without nulls ({@link com.google.common.io.Files.getFileNameWithoutExtension})
	 */
	public static String getSimpleName(String s) {
		int i = findExtension(s);
		return 0 > i ? s : s.substring(0, i);
	}
	
	public static File changeExtension(File f, String newExtension) {
		return new File(f.getParent(), changeExtension(f.getName(), newExtension));
	}
	public static String changeExtension(String s, String newExtension) {
		return getSimpleName(s) + DOT + newExtension;
	}
	
	static final char DOT = '.';
	static final int findExtension(String s) {
		return
				Preconditions.checkNotNull(s, "can't check extension of null filename")
				.lastIndexOf(DOT);
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
	
	/**
	 * a surprising omission from the guava API,
	 * found in neither {@link ByteStreams} nor {@link Files}.
	 * delegates to {@link ByteStreams#copy(InputStream, OutputStream)}.
	 */
	public static long save(InputStream from, File to) throws IOException {
		OutputStream os = new FileOutputStream(to); try {
			return ByteStreams.copy(from, os);
		} finally {
			os.close();
		}
	}
	/**
	 * a surprising omission from the guava API,
	 * found in neither {@link Resources} nor {@link Files}.
	 * delegates to {@link StreamUtil#copy(URL, OutputStream)}. 
	 */
	public static long save(URL from, File to) throws IOException {
		OutputStream os = new FileOutputStream(to); try {
			return StreamUtil.copy(from, os);
		} finally {
			os.close();
		}
	}
	
	public static String getCanonicalPathUnchecked(File f){
		Preconditions.checkNotNull(f, "cannot getCanonicalPath on null file");
		try {
			return f.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(
					String.format(
							"IOE thrown by getCanonicalPath(%s)",
							f.getAbsolutePath()),
					e);
		}
	}
}
