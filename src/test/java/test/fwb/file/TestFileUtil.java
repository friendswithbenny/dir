package test.fwb.file;

import org.fwb.file.FileUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFileUtil {
	@Test
	public void testGetSizeString() {
		assertEquals("1.0G", FileUtil.getSizeString(1234567890));
		assertEquals("941.0M", FileUtil.getSizeString(987654321));
	}
}
