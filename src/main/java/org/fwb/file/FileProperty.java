package org.fwb.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

/** a marker interface */
public interface FileProperty<T> extends Function<File, T> {
	/** the property name */
	String name();
	
	/** the value type */
	Class<?> type();
	
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
		name(String.class) {
			@Override
			public String apply(File f) {
				return f.getName();
			}
		},
		path(String.class) {
			@Override
			public String apply(File f) {
				try {
					return f.getCanonicalPath();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		},
		length(Long.class) {
			@Override
			public Long apply(File f) {
				return f.length();
			}
		},
		lengthString(String.class) {
			@Override
			public final String apply(File f) {
				return FileUtil.getSizeString((Long) length.apply(f));
			}
		},
		hidden(Boolean.class) {
			@Override
			public Boolean apply(File f) {
				return f.isHidden();
			}
		},
		lastModified(Long.class) {
			@Override
			public Long apply(File f) {
				return f.lastModified();
			}
		},
		lastModifiedDate(Date.class) {
			@Override
			public Date apply(File f) {
				return new Date((Long) lastModified.apply(f));
			}
		},
		lastModifiedString(String.class) {
			@Override
			public String apply(File f) {
				return FileUtil.seconds().format((Date) lastModifiedDate.apply(f));
			}
		},
		isDirectory(Boolean.class) {
			@Override
			public Boolean apply(File f) {
				return f.isDirectory();
			}
		},
		simpleName(String.class) {
			@Override
			public String apply(File f) {
				return FileUtil.getSimpleName(f.getName());
			}
		},
		extension(String.class) {
			@Override
			public String apply(File f) {
				return FileUtil.getExtension(f.getName());
			}
		};
		
		final Class<?> CLS;
		FileField(Class<?> cls) {
			CLS = cls;
		}
		
		public Class<?> type() {
			return CLS;
		}
		
		public <T> FileProperty<T> cast(Class<T> type) {
			// TODO should this be .equals instead?
			// i take some comfort in this ensuring the same classloader, too..
			Preconditions.checkArgument(type() == type,
					"FileField %s can't be cast to return %s (expected: %s)", this, type, type());
			
			@SuppressWarnings({ "unchecked", "rawtypes"})
			FileProperty<T> retVal = (FileProperty<T>) (FileProperty) this;
			return retVal;
		}
	}
}