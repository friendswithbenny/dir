package org.fwb.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import com.google.common.io.ByteStreams;

public class StreamUtil {
	/** @deprecated static utilities only */
	@Deprecated
	private StreamUtil() { }
	
	/**
	 * analogous to {@link com.google.common.io.Resources#copy(URL, OutputStream)},
	 * except with return value analogous to {@link ByteStreams.copy},
	 * to which this delegates.
	 */
	public static long copy(URL from, OutputStream to) throws IOException {
		InputStream is = from.openStream(); try {
			return ByteStreams.copy(is, to);
		} finally {
			is.close();
		}
	}
}
