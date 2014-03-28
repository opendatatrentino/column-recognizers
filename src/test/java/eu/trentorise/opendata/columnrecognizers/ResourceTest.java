/**
 * 
 */
package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;

import org.junit.Test;

/**
 * Tests the access to project resources.
 * 
 * @author Simon
 *
 */
public class ResourceTest {
	private static final String CONFIGURATION_PATH = "column-recognizers-test.txt";

	@Test
	public void testSpecificationFile() {
		URL url = getClass().getResource("/" + CONFIGURATION_PATH);
		assertNotNull(url);
	}

//	@Test
	public void testExtractExeFile() {
		final String exeName = "svm_classify";
		// TODO can't find resource - need to add platform extension ('.exe') to path
		final String exeResourcePath 
			= "/svm-light/" + PlatformUtils.getPlatformName() + "/" + exeName;
				
		File exeDestination = FileUtils.getTmpFile(exeName);
		URL exeResource = FileUtils.getResourceURL(exeResourcePath);
		
		assertFalse(exeDestination.exists());
		FileUtils.extractFromJar(exeResource, exeDestination);
		exeDestination.deleteOnExit();
		assertTrue(exeDestination.exists());
	}
	
}
