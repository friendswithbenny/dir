package org.fwb.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.google.common.base.Function;

/** a marker interface */
public interface FileProperty<T> extends Function<File, T> {
	/** the property name */
	String name();
	
	/**
	 * an enumeration of the various metadata fields describing a File.
	 * not all of these would conceivably be used at once,
	 * particularly as some are redundant with different formats.
	 * 
	 * e.g. the {@link #path} value is recommended for the *root* file(s) in any representation,
	 * but not for descendants.
	 * likewise, either {@link #length} or {@link #lengthString} might be used,
	 * but presumably not both
	 * (though sometimes one is desirable for display, and the other for sorting/comparison).
	 * 
	 * TODO add 'inputStream' and make a standard default-set
	 * e.g. without 'inputStream' and with only 'lengthString'
	 */
	enum FileField implements FileProperty<Object> {
		name {
			@Override
			public Object apply(File f) {
				return f.getName();
			}
		},
		path {
			@Override
			public String apply(File f) {
				try {
					return f.getCanonicalPath();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		},
		length {
			@Override
			public Long apply(File f) {
				return f.length();
			}
		},
		lengthString {
			@Override
			public final String apply(File f) {
				return FileUtil.getSizeString((Long) length.apply(f));
			}
		},
		hidden {
			@Override
			public Boolean apply(File f) {
				return f.isHidden();
			}
		},
		lastModified {
			@Override
			public Long apply(File f) {
				return f.lastModified();
			}
		},
		lastModifiedDate {
			@Override
			public Date apply(File f) {
				return new Date((Long) lastModified.apply(f));
			}
		},
		lastModifiedString {
			@Override
			public String apply(File f) {
				return FileUtil.seconds().format((Date) lastModifiedDate.apply(f));
			}
		},
		isDirectory {
			@Override
			public Boolean apply(File f) {
				return f.isDirectory();
			}
		},
		simpleName {
			@Override
			public String apply(File f) {
				return FileUtil.getSimpleName(f.getName());
			}
		},
		extension {
			@Override
			public String apply(File f) {
				return FileUtil.getExtension(f.getName());
			}
		};
	}
}