package org.fwb.file.dir;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.fwb.alj.col.SetUtil.SetView.ListSetView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class DirectoryUtility {
	private static final Logger LOG = LoggerFactory.getLogger(TempDirectory.class);
	
	@Deprecated private DirectoryUtility() {}
	
	// TODO remove (moved to FileFilters)
//	public static final FileFilter
//		FF_TRUE = new FileFilter() {
//			/** always returns true */
//			@Override
//			public boolean accept(File pathname) {
//				return true;
//			}
//		},
//		/** @deprecated warning: prompts with blocking JOptionPane */
//		FF_ASK = new FileFilter() {
//			@Override
//			public boolean accept(File pathname) {
//				LOG.warn("freezing popup ({})", pathname);
//				return 1 == JOptionPane.showOptionDialog(null, pathname, "accept file?",
//					JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"No", "Yes"}, "No");
//			}
//		};
	
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
	
	/**
	 * wraps {@link File#File(File, String)} in a {@link Function}.
	 * n.b. it is literally that wrapper, and provides no safeguards
	 * e.g. against ancestor, descendent, absolute, etc.
	 */
	public static class DirectoryFunction implements Function<String, File> {
		final File PARENT;
		public DirectoryFunction(File parent) {
			PARENT = parent;
			Preconditions.checkArgument(PARENT.isDirectory(),
					"parent must be directory (%s)", PARENT);
		}
		
		@Override
		public File apply(String fileName) {
			return new File(PARENT, fileName);
		}
		
		public Map<String, File> getDirectoryMap() {
			return Maps.asMap(
					new ListSetView<String>(Arrays.asList(
							PARENT.list())),
					this);
		}
	}
}
