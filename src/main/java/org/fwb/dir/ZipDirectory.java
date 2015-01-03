package org.fwb.dir;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * extends the TempDirectory API to include a source/target zip-file.
 * when done, the user must call {@link #close} on any instance of this class, to release its resources (temp-directory).
 * 
 * upon construction, an instance of this class works as a temporary directory.
 * this directory's contents are meant "to represent" the archived contents of the zip-file.
 * this class implements "synch" functionality between them in both directions:
 * {@link #zip} from the TempDirectory to the zip-file, and
 * {@link #unzip} from the zip-file into the TempDirectory.
 * 
 * optionally, the zip-file may be treated as a "source"
 * and unzipped into the directory *upon construction*.
 * likewise, the zip-file may be treated as "destination,"
 * and the user's actions/content in the directory are auto-zipped to the zip-file *upon close*.
 */
public class ZipDirectory extends TempDirectory {
	/** default */
	private static final long serialVersionUID = 1;
	
	private static final Logger LOG = LoggerFactory.getLogger(ZipDirectory.class);
	
	public final File ZIP;
	public final boolean ZIP_ON_CLOSE;
	public ZipDirectory(File zip) throws IOException {
		this(zip, zip.exists(), true, "ZipDirectory", "", null);
	}
	public ZipDirectory(File zip,
			boolean unzipOnCreate, boolean zipOnClose,
			String prefix, String suffix, File location) throws IOException {
		super(prefix, suffix, location); // IOException
		ZIP = zip;
		ZIP_ON_CLOSE = zipOnClose;
		LOG.trace("creating ZipDirectory({}, {}, {}, {}, {}, {})",
				zip, unzipOnCreate, zipOnClose, prefix, suffix, location);
		
		if (unzipOnCreate)
			unzip();
	}
	
	/**
	 * unzips the contents of File {@link #ZIP} into this directory
	 */
	public final void unzip() throws IOException {
		ZipUtility.unzip(ZIP, this);
	}
	/**
	 * sends the contents of this directory to File {@link #ZIP}
	 */
	public final void zip() throws IOException {
		ZipUtility.zip(ZIP, listFiles());
	}
	
	@Override
	public void close() throws IOException {
		if (ZIP_ON_CLOSE)
			zip();
		super.close();
	}
	
	@Override
	public String toString() {
		return super.toString() + " -> " + ZIP;
	}
}