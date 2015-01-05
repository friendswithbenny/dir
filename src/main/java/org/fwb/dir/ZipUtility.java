package org.fwb.dir;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

/**
 * integrates "zipping" functionality (archive + compress)
 * with simplified/familiar API for folders/directories
 * 
 * note: the Java Zip API is imperfect.
 *  empty folders are not preserved inside of a zip.
 *  particularly, zipping a directory and then unzipping it
 *  will not necessarily yield an identical structure.
 * 
 * TODO merge the two unzip implementations,
 *  using some uniform data structure for Stream/File;
 *  see guava ByteSource.
 */
public class ZipUtility {
	private static final Logger LOG = LoggerFactory.getLogger(ZipUtility.class);
	
	public static final char FAILSAFE_CHAR;
	static {
		Properties p = new Properties();
		try {
			InputStream is = ZipUtility.class.getResourceAsStream("ZipUtility.properties"); try {
				p.load(is);
			} finally {
				is.close();
			}
			String failsafeString =
					System.getProperty("org.fwb.dir.ZipUtility.FAILSAFE_CHAR",
							p.getProperty("FAILSAFE_CHAR"));
			if (failsafeString.length() != 1)
				throw new IllegalArgumentException("FAILSAFE_CHAR should be a single character: \"" + failsafeString + "\"");
			FAILSAFE_CHAR = failsafeString.charAt(0);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Deprecated private ZipUtility() { }
	
	/**
	 * facility method to auto-handle streaming to a zip File
	 * 
	 * @param outputZip the destination zip File
	 * @see #zip(OutputStream, File...)
	 */
	public static final void zip(File outputZip, File... inputContents) throws IOException {
		OutputStream os = new FileOutputStream(outputZip); try {
			zip(os, inputContents);
		} finally {
			os.close();
		}
	}
	
	/**
	 * writes to the given OutputStream a zip-File which contains the given files/hierarchies.
	 * any folder recursively has its contents added.
	 * 
	 * note: any empty folder will NOT be added.
	 * note: does NOT close OutputStream "zip" when done
	 * 
	 * @param outputZip the destination stream to which to send the zip's contents
	 * @param inputContents the Files to put into the zip
	 * 
	 * @throws IOException thrown by underlying streaming/zipping libraries
	 */
	public static final void zip(OutputStream outputZip, File... inputContents) throws IOException {
		LOG.debug("start zip({}, {})", outputZip, inputContents);
		ZipOutputStream zos = new ZipOutputStream(outputZip);
		for (File f : inputContents) {
			String root = f.getParent();
			/*
			 * necessary because actual roots (drives, like c:\)
			 * have canonical path ending in the separator,
			 * while all other directories do not
			 */
			if (! root.endsWith(File.separator))
				root = root + File.separator;
			addFileToZip(zos, root, f);
		}
		zos.finish();	// ZipException, IOException
		LOG.debug("end zip({}, {})", outputZip, inputContents);
	}
	/**
	 * adds a File, named relative to some context root path, to a zip container
	 * 
	 * note: this method does NOT add empty folders to the zip.
	 *  they are completely lost in translation.
	 * 
	 * @param zos the ZipOutputStream
	 * @param rootPath path of root, *including* trailing path separator.
	 *  all ZipEntries will have their names relative to this context root.
	 * @param content the File to add to the zip, recursively.
	 * 
	 * @throws IOException thrown by underlying streaming/zipping libraries
	 */
	private static final void addFileToZip(ZipOutputStream zos, String rootPath, File content) throws IOException {
		LOG.trace("start addFileToZip({}, {}, {})", zos, rootPath, content);
		String name = content.getPath().substring(rootPath.length());
		
		if (content.isDirectory()) {	
//			LOG.trace("adding directory: {}", content);
			for (File f : content.listFiles())
				addFileToZip(zos, rootPath, f);
		} else {
			LOG.trace("adding leaf: {}", content);
			
			/*
			 * TODO
			 * this is a temporary hack-fix, but needs to be better thought out.
			 * particularly, Zip7 can add m-dashes to names, and recognize them,
			 * while windows sees zip7's output as "G\u00c7\u00f6".
			 * Windows disallows adding m-dashes to a zip
			 * Java adds them, then windows also sees them as "G\u00c7\u00f6".
			 * In this case, even zip7 sees them as "G\u00c7\u00f6", so what is the difference?
			 */
			name = name.replace('\u2014', FAILSAFE_CHAR);	// replace em-dash with hyphen
			
			/*
			 * TODO
			 * windows nastyhack sadness :(
			 * there must at least be a way to "thoughtfully" acknowledge m$win before doing this?
			 * e.g. on non-windows platforms, i'd prefer to just throw IOE if contains \\
			 */
			name = name.replace('\\', '/');
			
			ZipEntry ze = new ZipEntry(name); // IllegalArgumentException: if name is longer than OxFFFF bytes
			zos.putNextEntry(ze); // ZipException, IOException
			InputStream is = new FileInputStream(content); try { // FileNotFoundException, IOException
				ByteStreams.copy(is, zos); // IOException
			} finally {
				is.close(); // IOException
			}
			zos.closeEntry(); // ZipException, IOException
		}
		LOG.trace("end addFileToZip({}, {}, {})", zos, rootPath, content);
	}
	
	/**
	 * unzip, over-writing any existing files that conflict.
	 * @see #unzip(File, File, FileFilter)
	 */
	public static final void unzip(File inputZip, File outputDirectory) throws IOException {
		unzip(inputZip, outputDirectory, DirectoryUtility.FF_TRUE);
	}
	/**
	 * unzips the contents of a zip File into a directory.
	 * 
	 * unlike its cousins the "zip(_)" methods,
	 * this correctly extracts empty folders,
	 * if they were correctly archived in the first place. 
	 *  
	 * @param inputZip the zip File to be unzipped
	 * @param outputDirectory the destination directory.
	 *  if this does not exist, it will be created
	 * @param overWrite filter to determine whether to over-write conflicting target file
	 */
	public static final void unzip(File inputZip, File outputDirectory, FileFilter overWrite) throws IOException {
		LOG.debug("start unzip({}, {}, {})", inputZip, outputDirectory, overWrite);
		
		ZipFile zipFile = new ZipFile(inputZip);
		
		for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements(); ) {
			ZipEntry entry = entries.nextElement();
			
			File f = new File(outputDirectory, entry.getName());
			if (entry.isDirectory()) {
				// necessary for empty directories, who will not be made by "getParentFile().mkdirs()" below
				LOG.trace("extracting directory: {} -> {}", entry, f);
				f.mkdirs();
			} else {
				LOG.trace("extracting leaf: {} -> {}", entry, f);
				
				// this is necessary for files in a top-level directory
				// because that top-level directory is not a ZipEntry
				f.getParentFile().mkdirs();	
				
				// handle collisions
				if (f.exists()) {
					if (overWrite.accept(f)) {
						LOG.trace("collision over-written: {}", f);
					} else {
						LOG.trace("collision filtered: {}", f);
						continue;
					}
				}
				
				// write the file
				InputStream is = zipFile.getInputStream(entry); // ZipException, IOException, IllegalStateException
				OutputStream os = new FileOutputStream(f); try { // FileNotFoundException, IOException
					ByteStreams.copy(is, os); // IOException
				} finally {
					os.close(); // IOException
				}
				is.close(); // IOException
			}
		}
		
		zipFile.close(); // IOException
		
		LOG.debug("end unzip({}, {})", inputZip, outputDirectory);
	}
	
	/**
	 * given an InputStream of zipped contents, unzips into the given directory.
	 * 
	 * note: the InputStream is NOT closed upon completion
	 */
	public static final void unzip(InputStream zip, File outputDirectory) throws IOException {
		LOG.debug("start unzip(" + zip + ", " + outputDirectory + ")");
		
		ZipInputStream zipFile = new ZipInputStream(zip);										// ZipException, IOException
		
		for (ZipEntry entry; (entry = zipFile.getNextEntry()) != null; ) {
			File f = new File(outputDirectory, entry.getName());
			if (entry.isDirectory()) {
				// necessary for empty directories, who will not be made by "getParentFile().mkdirs()" below
//				LOG.debug("extracting directory: " + f);
				f.mkdirs();
			} else {
//				LOG.debug("extracting file: " + f);
				
				// this is necessary for files in a top-level directory
				// because that top-level directory is not a ZipEntry
				f.getParentFile().mkdirs();	
				
				// write the file
				OutputStream os = new FileOutputStream(f); try {	// FileNotFoundException, IOException
					ByteStreams.copy(zipFile, os);					// IOException
				} finally {
					os.close();										// IOException
				}
			}
			zipFile.closeEntry();
		}
		
		zipFile.close();
		
		LOG.debug("done unzip(" + zip + ", " + outputDirectory + ")");
	}
}