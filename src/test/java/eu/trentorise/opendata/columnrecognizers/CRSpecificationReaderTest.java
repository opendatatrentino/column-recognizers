package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class CRSpecificationReaderTest {
	private static final String CONFIGURATION_PATH = "column-recognizers-test.txt";

	@Test
	public void test() {
		final String CSV_PATH = "prodotti_protetti.csv";
		final char COLUMN_SEPARATOR = ',';
		final int NUMBER_OF_COMPONENT_RECOGNIZERS = 11;
		
		File csvFile = new File(CSV_PATH); 
		RowTable rowTable = RowTable.loadFromCSV(csvFile, COLUMN_SEPARATOR);
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		CRSpecificationReader reader 
			= new CRSpecificationReader(
					new File(CONFIGURATION_PATH), 
					compositeCR, 
					rowTable, 
					rowTable.extractRowSample());
		reader.read();
		assertTrue(compositeCR.getComponentCount() == NUMBER_OF_COMPONENT_RECOGNIZERS);
	}

}
