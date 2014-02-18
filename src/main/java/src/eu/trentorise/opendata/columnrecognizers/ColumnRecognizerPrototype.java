package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ColumnRecognizerPrototype {
	private final static String DIVIDER = "________________________________________________________________";
	private final static String LONG_DIVIDER = DIVIDER + DIVIDER + DIVIDER;
	private final static String INVERSE_FREQUENCIES_PATH = "inverse-frequencies.txt";
	private final static int NUMBER_OF_HEADER_ROWS = 1;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ColumnRecognizerPrototype app = new ColumnRecognizerPrototype();
		app.runRecognizers();
//		app.testCSVProcessor();
//		app.testWordFrequencies();
//		app.readWordScores();
//		app.testTFIDF();
//		app.testClassifierFeatures();
	}

	private void runRecognizers() {
		final double CONFIDENCE_THRESHOLD = 0.1;
		final String SPECIFICATION_PATH = "column-recognizers.txt";
		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
//		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		final char COLUMN_SEPARATOR = ';';
//		final char COLUMN_SEPARATOR = ',';
	
		// Load CSV file
		File csvFile = new File(CSV_PATH); 
		RowTable table = RowTable.loadFromCSV(csvFile, COLUMN_SEPARATOR);
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		RowTable sample = table.extractSample();
		
		// Create recognizers from specification file
		FusionColumnRecognizer fusionCR 
			= new FusionColumnRecognizer("fusion", CONFIDENCE_THRESHOLD);
		File specificationFile = new File(SPECIFICATION_PATH);
		ColumnRecognizerFactory.attachRecognizers(fusionCR, specificationFile, table, sample);
		
		List<ColumnConceptCandidate> scoredCandidates = fusionCR.computeScoredCandidates();
		
		// Print scored candidates
		Iterator<ColumnConceptCandidate> it = scoredCandidates.iterator();
		while (it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}
	
	public void updateDataFiles(RowTable table) {
		// Print towns to file
		final int TOWN_COLUMN = 2;
		RowTable townColumn = table.extractColumn(TOWN_COLUMN);
		townColumn.writeNormalizedValueSetToFile(new File("comune.txt"));
		
		// Print frazione
		final int FRAZIONE_COLUMN = 5;
		RowTable frazioneColumn = table.extractColumn(FRAZIONE_COLUMN);
		frazioneColumn.writeNormalizedValueSetToFile(new File("frazione.txt"));
	}
	
	public void testCSVProcessor() {
		// Test that we get the rows correctly
//		final String CSV_PATH = "Punti-Interesse-Commerciali.csv";
//		final String CSV_PATH = "Impianti-Risalita-Vivifiemme.csv";
//		final String CSV_PATH = "Parcheggi-Vivifiemme.csv";
//		final String CSV_PATH = "Punti-di-interesse-sportivi-Vivifiemme.csv";
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
//		final String CSV_PATH = "Punti-Interesse-Esercizi-Ricettivi-Vivifiemme.csv";
//		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
		
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		
		Iterator<String> it = table.getRowIterator();
		
//		while (it.hasNext()) {
//			System.out.println(it.next());
//			System.out.println(LONG_DIVIDER);
//		}
		
		// Display header
		System.out.println(it.next());

		
		// Test column separation
		RowTable column = table.extractColumn(4);
		it = column.getRowIterator();
//		while (it.hasNext()) {
		for (int i = 0; i < 4; i++) {
			System.out.println(it.next());
			System.out.println(LONG_DIVIDER);
		}
	}

	private void testWordFrequencies() {
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		
		RowTable column = table.extractColumn(6);
		column.removeHeaders(NUMBER_OF_HEADER_ROWS);
		List<String> words = column.extractWords();
		
		System.out.println(words.toString());
		
		Map<String, Integer> frequencies = column.computeWordFrequencies();
		System.out.println(frequencies.toString());
		
		Set<String> wordSet = column.extractWordSet();
		System.out.println(wordSet.toString());
	}

	private void readWordScores() {
		WordScoreReader reader = new WordScoreReader(new File(INVERSE_FREQUENCIES_PATH));
		reader.read();
		System.out.println(reader.getWordScores().toString());
	}
	
	private void testTFIDF() {
		final int ID_COLUMN = 1;
		final int TYPE_COLUMN = 2;
		final int RESTAURANT_NAME_COLUMN = 3;
		final int ADDRESS_COLUMN = 4;
		final String RESTAURANT_TFIDF_PATH = "italian_restaurant_tfidf.txt";
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		
//		System.out.println(table.getRowIterator().next());
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		RowTable restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		RowTable typeColumn = table.extractColumn(TYPE_COLUMN);
		RowTable idColumn = table.extractColumn(ID_COLUMN);
		RowTable addressColumn = table.extractColumn(ADDRESS_COLUMN);
		
		InverseColumnFrequency inverseFrequencies 
			= InverseColumnFrequency.readFromFile(new File(INVERSE_FREQUENCIES_PATH));
		TFIDFVector restaurantVector = new TFIDFVector(restaurantColumn, inverseFrequencies);
//		restaurantVector.writeToFile(new File(RESTAURANT_TFIDF_PATH));
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
	
	private void testClassifierFeatures() {
		InverseColumnFrequency inverseFrequencies 
			= InverseColumnFrequency.readFromFile(new File(INVERSE_FREQUENCIES_PATH));

		TFIDFVector viviFiemmeRestaurantVector 
			= computeViviFiemmeRestaurantFeatures(inverseFrequencies);
		System.out.println();
		computeOsterieFeatures(inverseFrequencies, viviFiemmeRestaurantVector);
	}

	private void computeOsterieFeatures(InverseColumnFrequency inverseFrequencies,
			TFIDFVector prototypeRestaurantVector) {
		final int ID_COLUMN = 1;
		final int TYPE_COLUMN = 4;
		final int RESTAURANT_NAME_COLUMN = 3;
		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
		
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ';');
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		RowTable restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		RowTable typeColumn = table.extractColumn(TYPE_COLUMN);
		RowTable idColumn = table.extractColumn(ID_COLUMN);

		TFIDFVector restaurantVector = new TFIDFVector(restaurantColumn, inverseFrequencies);
		TFIDFVector typeVector = new TFIDFVector(typeColumn, inverseFrequencies);
		TFIDFVector idVector = new TFIDFVector(idColumn, inverseFrequencies);

		System.out.println(LONG_DIVIDER);
		System.out.println("Osterie");
		System.out.println();

		double typeSimilarity = prototypeRestaurantVector.cosineSimilarity(typeVector);
		double restaurantSimilarity = prototypeRestaurantVector.cosineSimilarity(restaurantVector);
		double idSimilarity = prototypeRestaurantVector.cosineSimilarity(idVector);
		double typeUniqueness = typeColumn.getUniqueness();
		double restaurantUniqueness = restaurantColumn.getUniqueness();
		double idUniqueness = idColumn.getUniqueness();
		System.out.println(String.format(Locale.US, "Prototype similarity type column: %f", 
				typeSimilarity));
		System.out.println(String.format(Locale.US, "Prototype similarity restaurant column: %f", 
				restaurantSimilarity));
		System.out.println(String.format(Locale.US, "Prototype similarity id column: %f", 
				idSimilarity));
		System.out.println(String.format(Locale.US, "Uniqueness type column: %f", 
				typeUniqueness));
		System.out.println(String.format(Locale.US, "Uniqueness restaurant column: %f", 
				restaurantUniqueness));
		System.out.println(String.format(Locale.US, "Uniqueness id column: %f", 
				idUniqueness));
		
		System.out.println(LONG_DIVIDER);
		System.out.println(String.format(Locale.US, "-1 1:%f 2:%f # Osterie Restaurant type", 
				typeSimilarity, 
				typeUniqueness));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Osterie Restaurant name", 
				restaurantSimilarity, 
				restaurantUniqueness));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Osterie ID", 
				idSimilarity, 
				idUniqueness));
	}

	private TFIDFVector computeViviFiemmeRestaurantFeatures(InverseColumnFrequency inverseFrequencies) {
		final int ID_COLUMN = 1;
		final int TYPE_COLUMN = 2;
		final int RESTAURANT_NAME_COLUMN = 3;
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		RowTable restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		RowTable typeColumn = table.extractColumn(TYPE_COLUMN);
		RowTable idColumn = table.extractColumn(ID_COLUMN);
		
		TFIDFVector restaurantVector = new TFIDFVector(restaurantColumn, inverseFrequencies);
		TFIDFVector typeVector = new TFIDFVector(typeColumn, inverseFrequencies);
		TFIDFVector idVector = new TFIDFVector(idColumn, inverseFrequencies);
		TFIDFVector prototypeRestaurantVector = restaurantVector;

		System.out.println(LONG_DIVIDER);
		System.out.println("Vivifiemme");
		System.out.println();
		
		double typeSimilarity = prototypeRestaurantVector.cosineSimilarity(typeVector);
		double restaurantSimilarity = prototypeRestaurantVector.cosineSimilarity(restaurantVector);
		double idSimilarity = prototypeRestaurantVector.cosineSimilarity(idVector);
		double typeUniqueness = typeColumn.getUniqueness();
		double restaurantUniqueness = restaurantColumn.getUniqueness();
		double idUniqueness = idColumn.getUniqueness();
		System.out.println(String.format(Locale.US, "Prototype similarity type column: %f", 
				typeSimilarity));
		System.out.println(String.format(Locale.US, "Prototype similarity restaurant column: %f", 
				restaurantSimilarity));
		System.out.println(String.format(Locale.US, "Prototype similarity id column: %f", 
				idSimilarity));
		System.out.println(String.format(Locale.US, "Uniqueness type column: %f", 
				typeUniqueness));
		System.out.println(String.format(Locale.US, "Uniqueness restaurant column: %f", 
				restaurantUniqueness));
		System.out.println(String.format(Locale.US, "Uniqueness id column: %f", 
				idUniqueness));
		
		System.out.println(LONG_DIVIDER);
		System.out.println(String.format(Locale.US, "-1 1:%f 2:%f # Vivifiemme Restaurant type", 
				typeSimilarity, 
				typeUniqueness));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Vivifiemme Restaurant name", 
				restaurantSimilarity, 
				restaurantUniqueness));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Vivifiemme ID", 
				idSimilarity, 
				idUniqueness));

		return restaurantVector;
	}
}
