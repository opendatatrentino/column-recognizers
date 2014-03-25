package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the static ColumnRecognizer APIs.
 * 
 * @author Simon
 *
 */
public class APITest {
	/**
	 * Path for finding the "Impianti di risalita" table in the test resources
	 */
	private static final String IMPIANTI_CSV_RESOURCE_PATH = "/tables/Impianti-Risalita-Vivifiemme.csv";
	
	/**
	 * Column separator for the Impianti CSV data table
	 */
	private static final char IMPIANTI_COLUMN_SEPARATOR = ',';
	
	/**
	 * Path for finding the "Prodotti certificati" table in the test resources
	 */
	private static final String PRODOTTI_CSV_RESOURCE_PATH = "/tables/prodotti_protetti.csv";
	
	/**
	 * Column separator for the Prodotti CSV data table
	 */
	private static final char PRODOTTI_COLUMN_SEPARATOR = ',';
	
	/**
	 * Path to alternative column recognizer specification file
	 */
	private static final String ALTERNATIVE_SPEC_FILE_RESOURCE_PATH 
		= "/extra/column-recognizers-extra-test.txt";
	
	/**
	 * Path to alternative column recognizer specification file with custom
	 * model
	 */
	private static final String ALTERNATIVE_SPEC_FILE_2_RESOURCE_PATH
		= "/extra/column-recognizers-extra-test-2.txt";
	
	/**
	 * Path to model directory
	 */
	private static final String MODEL_DIRECTORY_RESOURCE_PATH = "/extra";
	
	/**
	 * Minimal number of column-concept candidates
	 */
	private static final int MINIMUM_CANDIDATE_COUNT = 5;
	
	/**
	 * Impianti table headers
	 */
	private List<String> impiantiHeaders = null;
	
	/**
	 * Impianti table columns
	 */
	private List<Column> impiantiColumns = null;
	
	/**
	 * Prodotti table headers
	 */
	private List<String> prodottiHeaders = null;
	
	/**
	 * Prodotti table columns
	 */
	private List<Column> prodottiColumns = null;

	/**
	 * Constructs the header and column data used in the tests.
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		RowTable impiantiTable = loadTable(IMPIANTI_CSV_RESOURCE_PATH, IMPIANTI_COLUMN_SEPARATOR);
		impiantiHeaders = impiantiTable.popHeaders();
		impiantiColumns = impiantiTable.extractColumns();	
		
		RowTable prodottiTable = loadTable(PRODOTTI_CSV_RESOURCE_PATH, PRODOTTI_COLUMN_SEPARATOR);
		prodottiHeaders = prodottiTable.popHeaders();
		prodottiColumns = prodottiTable.extractColumns();	
	}
	
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


	/**
	 * Tests the ColumnRecognizer.conceptFromText API.
	 */
	@Test
	public void testConceptFromText() {
		final long IMPIANTI_CONCEPT_ID = 14557;
		long conceptID = ColumnRecognizer.conceptFromText("impianti risalita");
		assertTrue(conceptID == IMPIANTI_CONCEPT_ID);
	}

	/**
	 * Tests the ColumnRecognizer.computeScoredCandidates API with the minimal
	 * number of parameters.
	 */
	@Test
	public void testSimpleAPI() {
		List<ColumnConceptCandidate> scoredCandidates 
			= ColumnRecognizer.computeScoredCandidates(
					impiantiHeaders, 
					Column.toStringLists(impiantiColumns));
		assertTrue(scoredCandidates.size() >= MINIMUM_CANDIDATE_COUNT);
	}
	
	/**
	 * Tests the ColumnRecognizer.computeScoredCandidates API with added 
	 * parameters.
	 */
	@Test
	public void testComplexAPI() {
		URL url = getClass().getResource(ALTERNATIVE_SPEC_FILE_RESOURCE_PATH);
		File alternativeSpecFile = new File(url.getPath()); 
		List<ColumnConceptCandidate> scoredCandidates 
			= ColumnRecognizer.computeScoredCandidates(
				prodottiHeaders, 
				Column.toStringLists(prodottiColumns), 
				alternativeSpecFile);
		assertTrue(scoredCandidates.size() >= MINIMUM_CANDIDATE_COUNT);
		
		url = getClass().getResource(ALTERNATIVE_SPEC_FILE_2_RESOURCE_PATH);
		alternativeSpecFile = new File(url.getPath()); 
		url = getClass().getResource(MODEL_DIRECTORY_RESOURCE_PATH);		
		File modelDirectory = new File(url.getPath()); 
		List<File> modelDirectories = new ArrayList<File>();
		modelDirectories.add(modelDirectory);
		scoredCandidates = ColumnRecognizer.computeScoredCandidates(
				prodottiHeaders, 
				Column.toStringLists(prodottiColumns), 
				alternativeSpecFile,
				modelDirectories);
		assertTrue(scoredCandidates.size() >= MINIMUM_CANDIDATE_COUNT);
		
	}
	

}
