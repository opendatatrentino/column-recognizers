package eu.trentorise.opendata.columnrecognizers;
import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import it.unitn.disi.sweb.webapi.client.IProtocolClient;
import it.unitn.disi.sweb.webapi.client.ProtocolFactory;
import it.unitn.disi.sweb.webapi.client.nlp.PipelineClient;
import it.unitn.disi.sweb.webapi.model.NLPInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TestBench {
        private static final Logger LOG = LoggerFactory.getLogger(TestBench.class);    
	private final static String DIVIDER = "________________________________________________________________";
	private final static String LONG_DIVIDER = DIVIDER + DIVIDER + DIVIDER;
	private final static String INVERSE_FREQUENCIES_PATH = "/models/inverse-frequencies.txt";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TestBench app = new TestBench();
		
		if (args.length < 3) {
			app.runRecognizers();			
		} else if (args.length == 3) {
			String csvPath = args[0];
			char columnSeparator = args[1].charAt(0);
			File specificationFile = new File(args[2]);
			app.runRecognizers(
					new File(csvPath), 
					columnSeparator, 
					specificationFile);
		}
	
//		System.out.println(ColumnRecognizer.conceptFromText("civico"));
//		app.testCSVProcessor();
//		app.testWordFrequencies();
//		app.readWordScores();
//		app.testTFIDF();
//		app.testWebAPI();
	}

	private void runRecognizers(File csvFile, char columnSeparator, File specificationFile) {
		RowTable rowTable = RowTable.loadFromCSV(csvFile, columnSeparator);
		List<String> headers = rowTable.popHeaders();
		List<Column> columns = rowTable.extractColumns();
		List<List<String>> columnData = Column.toStringLists(columns);
		List<ColumnConceptCandidate> scoredCandidates = null;

		if (specificationFile == null) {
			scoredCandidates = ColumnRecognizer.computeScoredCandidates(headers, columnData);
		} else {
			File modelDirectory = new File(specificationFile.getParentFile(), "models");
			List<File> modelDirectories = new ArrayList<File>();
			modelDirectories.add(modelDirectory);
			InputStream specificationStream = null;
			try {
				specificationStream = new FileInputStream(specificationFile);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			scoredCandidates  
				= ColumnRecognizer.computeScoredCandidates(
					headers, 
					columnData, 
					specificationStream, 
					modelDirectories);
		}

		Iterator<ColumnConceptCandidate> it = scoredCandidates.iterator();
		while (it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}

//	private void runRecognizers(File csvFile, char columnSeparator) {
//		RowTable rowTable = RowTable.loadFromCSV(csvFile, columnSeparator);
//		List<String> headers = rowTable.popHeaders();
//				
//		List<Column> columns = rowTable.extractColumns();
//		List<List<String>> columnData = Column.toStringLists(columns);
//		List<ColumnConceptCandidate> scoredCandidates 
//			= ColumnRecognizer.computeScoredCandidates(headers, columnData);
//		
//		// Print scored candidates
//		Iterator<ColumnConceptCandidate> it = scoredCandidates.iterator();
//		while (it.hasNext()) {
//			System.out.println(it.next().toString());
//		}
//	}

	private void runRecognizers() {
//		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
//		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
//		final String CSV_PATH = "/tables/prodotti_protetti.csv";
		final String CSV_PATH = "/tables/Impianti-Risalita-Vivifiemme.csv";
//		final char COLUMN_SEPARATOR = ';';
		final char COLUMN_SEPARATOR = ',';
	
		File csvFile = FileUtils.getResourceFile(CSV_PATH);
		runRecognizers(csvFile, COLUMN_SEPARATOR, null);
	}

	// TODO Replace with interface (on ValueSetCR) to dump value sets to files
	public void updateDataFiles(RowTable table) {
		// Print towns to file
		final int TOWN_COLUMN = 2;
		Column townColumn = table.extractColumn(TOWN_COLUMN);
		townColumn.writeNormalizedValueSetToFile(new File("comune.txt"));
		
		// Print frazione
		final int FRAZIONE_COLUMN = 5;
		Column frazioneColumn = table.extractColumn(FRAZIONE_COLUMN);
		frazioneColumn.writeNormalizedValueSetToFile(new File("frazione.txt"));
	}
	
	public void testCSVProcessor() {
		// Test that we get the rows correctly
//		final String CSV_PATH = "Punti-Interesse-Commerciali.csv";
		final String CSV_PATH = "/tables/Impianti-Risalita-Vivifiemme.csv";
//		final String CSV_PATH = "Parcheggi-Vivifiemme.csv";
//		final String CSV_PATH = "Punti-di-interesse-sportivi-Vivifiemme.csv";
//		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
//		final String CSV_PATH = "Punti-Interesse-Esercizi-Ricettivi-Vivifiemme.csv";
//		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
		
		RowTable table = RowTable.loadFromCSV(FileUtils.getResourceFile(CSV_PATH), ',');
		
		Iterator<String> it = table.getRowIterator();
		
//		while (it.hasNext()) {
//			System.out.println(it.next());
//			System.out.println(LONG_DIVIDER);
//		}
		
		// Display header
		System.out.println(it.next());

		
		// Test column separation
		Column column = table.extractColumn(4);
		it = column.getFieldIterator();
//		while (it.hasNext()) {
		for (int i = 0; i < 4; i++) {
			System.out.println(it.next());
			System.out.println(LONG_DIVIDER);
		}
	}

	private void testWordFrequencies() {
		final String CSV_PATH = "/tables/Punti-di-ristoro-ViviFiemme.csv";
		RowTable table = RowTable.loadFromCSV(FileUtils.getResourceFile(CSV_PATH), ',');
		table.removeHeaders();
		Column column = table.extractColumn(6);
		List<String> words = column.extractWords();
		
		System.out.println(words.toString());
		
		Map<String, Integer> frequencies = column.computeWordFrequencies();
		System.out.println(frequencies.toString());
		
		Set<String> wordSet = column.extractWordSet();
		System.out.println(wordSet.toString());
	}

	private void readWordScores() {
		WordScoreReader reader 
			= new WordScoreReader(FileUtils.getResourceFile(INVERSE_FREQUENCIES_PATH));
		reader.read();
		System.out.println(reader.getWordScores().toString());
	}
	
	private void testTFIDF() {
		final int ID_COLUMN = 1;
		final int TYPE_COLUMN = 2;
		final int RESTAURANT_NAME_COLUMN = 3;
		final int ADDRESS_COLUMN = 4;
		final String CSV_PATH = "/tables/Punti-di-ristoro-ViviFiemme.csv";
		RowTable table = RowTable.loadFromCSV(FileUtils.getResourceFile(CSV_PATH), ',');
		
		table.removeHeaders();
		Column restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		Column typeColumn = table.extractColumn(TYPE_COLUMN);
		Column idColumn = table.extractColumn(ID_COLUMN);
		Column addressColumn = table.extractColumn(ADDRESS_COLUMN);
		
		InverseColumnFrequency inverseFrequencies 
			= InverseColumnFrequency.readFromFile(
					FileUtils.getResourceFile(INVERSE_FREQUENCIES_PATH));
		TFIDFVector restaurantVector = new TFIDFVector(restaurantColumn, inverseFrequencies);
		TFIDFVector typeVector = new TFIDFVector(typeColumn, inverseFrequencies);
		TFIDFVector idVector = new TFIDFVector(idColumn, inverseFrequencies);
		TFIDFVector addressVector = new TFIDFVector(addressColumn, inverseFrequencies);
		
		System.out.println(String.format(Locale.US, "Similarity with type column: %f", 
				restaurantVector.cosineSimilarity(typeVector)));
		System.out.println(String.format(Locale.US, "Similarity with self: %f", 
				restaurantVector.cosineSimilarity(restaurantVector)));
		System.out.println(String.format(Locale.US, "Similarity with id column: %f", 
				restaurantVector.cosineSimilarity(idVector)));
		System.out.println(String.format(Locale.US, "Similarity with address column: %f", 
				restaurantVector.cosineSimilarity(addressVector)));
		
		System.out.println(String.format(Locale.US, "Uniqueness type column: %f", 
				typeColumn.getUniqueness()));
		System.out.println(String.format(Locale.US, "Uniqueness restaurant column: %f", 
				restaurantColumn.getUniqueness()));
		System.out.println(String.format(Locale.US, "Uniqueness id column: %f", 
				idColumn.getUniqueness()));

	}
		
	private void testWebAPI() {
//		IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH,
//				"ui.disi.unitn.it", 8092);
        LOG.warn("TODO - USING HARDCODED ENGLISH WHEN CREATING SWEB CLIENT IN  testWebAPI");
        IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH);
        PipelineClient pipelineClient = new PipelineClient(api);

        List<String> text = new ArrayList<String>();
        text.add("indirizzo");
        text.add("comune");
        text.add("descIt");

        NLPInput input = new NLPInput();
        input.setText(text);

        NLText[] result = pipelineClient.run("KeywordTextPipeline", input, 1l);
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		int columnNumber = 1;
        for (NLText nlText : result) {
            System.out.println(nlText.toString());
			Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
			candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, "test", meanings));
			columnNumber++;
        }		
		System.out.println(candidates.toString());
	}

}
