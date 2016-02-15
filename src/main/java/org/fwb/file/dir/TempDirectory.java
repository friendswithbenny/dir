package org.fwb.file.dir;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * an extension of {@link File#createTempFile} to create a directory rather than a leaf.
 * an instance of this class is a File referencing said temp directory.
 */
public class TempDirectory extends File implements Closeable {
	/** default */
	private static final long serialVersionUID = 1;
	
	static final Logger LOG = LoggerFactory.getLogger(TempDirectory.class);
	
	protected static final String
		DEFAULT_PREFIX = "TempDirectory",
		DEFAULT_SUFFIX = ".tmpdir";
	protected static final File
		DEFAULT_LOCATION = null;
	
	public TempDirectory() throws IOException {
		this(DEFAULT_PREFIX);
	}
	public TempDirectory(String prefix) throws IOException {
		this(prefix, DEFAULT_SUFFIX);
	}
	public TempDirectory(String prefix, String suffix) throws IOException {
		this(prefix, suffix, DEFAULT_LOCATION);
	}
	
	/**
	 * uses {@link File#createTempFile(String, String, File)} to create a temporary file, deletes it immediately,
	 * and then creates (and encapsulates) an empty directory in its place (by canonical path).
	 * 
	 * @param prefix {@code prefix} passed as first argument to File.createTempFile
	 * @param suffix <code>suffix</code> passed as second argument to File.createTempFile
	 * @param location <code>directory</code> passed as third argument to File.createTempFile
	 * @throws IOException thrown by File.createTempfile or {@link File#getCanonicalPath()}
	 */
	public TempDirectory(String prefix, String suffix, File location) throws IOException {
		super(File.createTempFile(prefix, suffix, location).getCanonicalPath());
		if (! delete())
			throw new IOException("unable to delete temp-leaf: " + this);
		if (! mkdir())
			throw new IOException("unable to create temp-dir: " + this);
		
		LOG.trace("created TempDirectory({}, {}, {}): {}", prefix, suffix, location, this);
	}
	
	/**
	 * deletes this directory, and all of its contents recursively, in the filesystem.
	 * 
	 * @throws IOException if deleteDirectory returns false
	 * @see DirectoryUtility#deleteDirectory(File)
	 */
	@Override
	public void close() throws IOException {
		if (! DirectoryUtility.deleteDirectory(this))
			throw new IOException("could not deleteDirectory: " + this);
	}
}
