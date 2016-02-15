package test.fwb.file.dir;

import java.io.File;
import java.io.IOException;

import org.fwb.file.dir.TempDirectory;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTempDirectory {
	static Logger LOG = LoggerFactory.getLogger(TestTempDirectory.class);
	
	/** example usage */
	@Test
	public void testTempDirectory() throws Exception {
		TempDirectory td = new TempDirectory(); try {
			LOG.debug("created directory: " + td.getCanonicalPath());
			Assert.assertTrue(
					"the TempDirectory should (1) exist and (2) be a directory",
					td.isDirectory());
			Assert.assertTrue(
					"the TempDirectory should be easily writeable (create child)",
					new File(td, "foo.test").createNewFile());
		} finally {
			try {
				td.close();
			} catch (IOException e) {
				Assert.fail("the TempDirectory should close successfully (recursive delete): " + e);
			}
			Assert.assertFalse(
					"the TempDirectory shouldn't exist after calling close",
					td.exists());
		}
	}
}
