package org.fwb.dir;

import java.io.File;
import java.io.FileFilter;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DirectoryUtility {
	private static final Logger LOG = LoggerFactory.getLogger(TempDirectory.class);
	
	@Deprecated private DirectoryUtility() {}
	
	public static final FileFilter
		FF_TRUE = new FileFilter() {
			/** always returns true */
			@Override
			public boolean accept(File pathname) {
				return true;
			}
		},
		/** @deprecated warning: prompts with blocking JOptionPane */
		FF_ASK = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				LOG.warn("freezing popup ({})", pathname);
				return 1 == JOptionPane.showOptionDialog(null, pathname, "accept file?",
					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"No", "Yes"}, "No");
			}
		};
	
	/**
	 * recursively deletes a directory, deleting the files inside it and then itself
	 * returns true if the deletion succeeded, false otherwise
	 * 
	 * note: returning false, some files within the directory could've still deleted successfully
	 */
	public static final boolean deleteDirectory(File dir) {
		// recurse
		if (dir.isDirectory())
			for (File f : dir.listFiles())
				if (! deleteDirectory(f))
					return false;
		
		// delete the now-empty directory
		boolean retVal = dir.delete();
		if (! retVal)
			LOG.error("unable to delete {}", dir);
		return retVal;
	}
}
