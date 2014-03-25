/**
 * 
 */
package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;

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
	public void test() {
		URL url = getClass().getResource("/" + CONFIGURATION_PATH);
		assertNotNull(url);
	}

}
