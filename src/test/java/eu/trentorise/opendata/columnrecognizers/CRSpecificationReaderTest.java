package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

public class CRSpecificationReaderTest {
	private static final String CONFIGURATION_PATH = "column-recognizers-test.txt";
	
	/**
	 * Path for finding the "Prodotti certificati" table in the test resources
	 */
	private static final String PRODOTTI_CSV_RESOURCE_PATH = "/tables/prodotti_protetti.csv";
	
	/**
	 * Column separator for the Prodotti CSV data table
	 */
	private static final char PRODOTTI_COLUMN_SEPARATOR = ',';
	
	/**
	 * Loads a table from test resources.
	 * 
	 * @param resourcePath		The CSV table resource path
	 * @param columnSeparator	The CSV table column separator character
	 * @return					The table
	 */
	private RowTable loadTable(String resourcePath, char columnSeparator) {
		URL url = getClass().getResource(resourcePath);
		File csvFile = new File(url.getPath()); 
		return RowTable.loadFromCSV(csvFile, columnSeparator);
	}

	@Test
	public void test() {
		final int NUMBER_OF_COMPONENT_RECOGNIZERS = 12;
		
		RowTable rowTable = loadTable(PRODOTTI_CSV_RESOURCE_PATH, PRODOTTI_COLUMN_SEPARATOR);
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		InputStream stream = getClass().getResourceAsStream("/" + CONFIGURATION_PATH);
		CRSpecificationReader reader 
			= new CRSpecificationReader(
					stream,
					null,
					compositeCR, 
					rowTable, 
					rowTable.extractRowSample());
		reader.read();
		assertTrue(compositeCR.getComponentCount() == NUMBER_OF_COMPONENT_RECOGNIZERS);
	}

}
