package org.fwb.file.dir;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * support for the antiquated {@link FileFilter} and {@link FilenameFilter} interfaces.
 * whenever possible, proper algebraic interfaces like {@link Predicate} should be used instead;
 * however, this class convert between the APIs, when the older is necessary.
 */
public class FileFilters {
	static final Logger LOG = LoggerFactory.getLogger(FileFilters.class);
	
	public static final FileFilter
			FF_TRUE = new PredicateFileFilter(Predicates.<File>alwaysTrue());
	
	/** @deprecated warning: prompts with blocking JOptionPane */
	static final FileFilter FF_ASK = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			LOG.warn("freezing popup ({})", pathname);
			return 1 == JOptionPane.showOptionDialog(null, pathname, "accept file?",
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"No", "Yes"}, "No");
		}
	};
	
	public static FileFilter toFileFilter(final FilenameFilter fnf) {
		return new PredicateFileFilter(new FilenameFilterPredicate(fnf));
		
		// no-guava implementation
//		return new FileFilter() {
//			@Override
//			public boolean accept(File pathname) {
//				return fnf.accept(pathname.getParentFile(), pathname.getName());
//			}
//		};
	}
	public static FilenameFilter toFilenameFilter(final FileFilter ff) {
		return new PredicateFileFilter(new FileFilterPredicate(ff));
		
		// no-guava implementation
//		return new FilenameFilter() {
//			@Override
//			public boolean accept(File dir, String name) {
//				return ff.accept(new File(dir, name));
//			}
//		};
	}
	
	public static class PredicateFileFilter implements FileFilter, FilenameFilter {
		final Predicate<File> PREDICATE;
		public PredicateFileFilter(Predicate<File> predicate) {
			PREDICATE = predicate;
		}
		@Override
		public boolean accept(File dir, String name) {
			return PREDICATE.apply(new File(dir, name));
		}
		@Override
		public boolean accept(File pathname) {
			return PREDICATE.apply(pathname);
		}
	}
	
	public static class FileFilterPredicate implements Predicate<File> {
		final FileFilter FF;
		public FileFilterPredicate(FileFilter ff) {
			FF = ff;
		}
		@Override
		public boolean apply(File input) {
			return FF.accept(input);
		}
	}
	public static class FilenameFilterPredicate implements Predicate<File> {
		final FilenameFilter FNF;
		public FilenameFilterPredicate(FilenameFilter fnf) {
			FNF = fnf;
		}
		@Override
		public boolean apply(File input) {
			return FNF.accept(input.getParentFile(), input.getName());
		}
	}
}
