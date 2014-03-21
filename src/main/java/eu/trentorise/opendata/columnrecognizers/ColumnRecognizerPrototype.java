package eu.trentorise.opendata.columnrecognizers;
import it.unitn.disi.sweb.core.common.utils.ContextLoader;
import it.unitn.disi.sweb.core.kb.IKnowledgeBaseService;
import it.unitn.disi.sweb.core.kb.model.KnowledgeBase;
import it.unitn.disi.sweb.core.kb.model.vocabularies.Vocabulary;
import it.unitn.disi.sweb.core.nlp.INLPPipeline;
import it.unitn.disi.sweb.core.nlp.components.chunkers.Chunker;
import it.unitn.disi.sweb.core.nlp.components.chunkers.IChunker;
import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLText;
import it.unitn.disi.sweb.core.nlp.parameters.NLPParameters;
import it.unitn.disi.sweb.core.nlp.pipelines.ODHPipeline;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.trentorise.opendata.nlprise.DataTypeGuess;
import eu.trentorise.opendata.nlprise.DataTypeGuess.Datatype;
import eu.trentorise.opendata.nlprise.typecheckers.EmptyTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.JsonTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.ListTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.XmlTypeChecker;

import it.unitn.disi.sweb.webapi.client.IProtocolClient;
import it.unitn.disi.sweb.webapi.client.ProtocolFactory;
import it.unitn.disi.sweb.webapi.client.nlp.PipelineClient;
import it.unitn.disi.sweb.webapi.model.ComponentDescription;
import it.unitn.disi.sweb.webapi.model.PipelineDescription;
import it.unitn.disi.sweb.webapi.model.NLPInput;
//import it.unitn.disi.sweb.webapi.model.*;



@Component
@Scope("singleton")
public class ColumnRecognizerPrototype {
    @Autowired
    @Qualifier("ODHPipeline")
    private INLPPipeline<NLPParameters> headerPipeline;
    
    @Autowired
    @Qualifier("Chunker")
    private IChunker<NLPParameters> chunker;

//    @Autowired
//    @Qualifier("ColumnRecognizerPrototype")
//    private static ColumnRecognizerPrototype app;
	
	private final static String DIVIDER = "________________________________________________________________";
	private final static String LONG_DIVIDER = DIVIDER + DIVIDER + DIVIDER;
	private final static String INVERSE_FREQUENCIES_PATH = "inverse-frequencies.txt";
	private final static int NUMBER_OF_HEADER_ROWS = 1;
	private final static String SPECIFICATION_PATH = "column-recognizers.txt";
	

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
//		app.testFusionClassifier();
//        ContextLoader cl = new ContextLoader();
//        IChunker<NLPParameters> chunker = cl.getApplicationContext().getBean(Chunker.class);
//        ColumnRecognizerPrototype app = cl.getApplicationContext().getBean(ColumnRecognizerPrototype.class);
//		app.testNLPPipeline();
//		app.testWebAPI();
//		app.testTypeDectection();
//		app.testConceptFromText();
	}

	private void testConceptFromText() {
		long conceptID = ColumnRecognizer.conceptFromText("impianti risalita");
		System.out.println(conceptID);
	}

	private void runRecognizers() {
//		final double CONFIDENCE_THRESHOLD = 0.1;
//		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
//		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		final String CSV_PATH = "prodotti_protetti.csv";
//		final String CSV_PATH = "Impianti-Risalita-Vivifiemme.csv";
//		final char COLUMN_SEPARATOR = ';';
		final char COLUMN_SEPARATOR = ',';
	
		// Load CSV file
		File csvFile = new File(CSV_PATH); 
		RowTable rowTable = RowTable.loadFromCSV(csvFile, COLUMN_SEPARATOR);
		String headerRow = rowTable.getRowIterator().next();
		List<String> headers = new ArrayList<String>(Arrays.asList(
					CSVProcessor.splitRecord(headerRow, COLUMN_SEPARATOR)));
		rowTable.removeHeaders(NUMBER_OF_HEADER_ROWS);
				
//		ColumnTable columnTable = new ColumnTable(null, rowTable);		
//		List<ColumnConceptCandidate> scoredCandidates = recognizeTable(columnTable);
		
		List<List<String>> columnData = getColumnsAsLists(rowTable);
		List<ColumnConceptCandidate> scoredCandidates 
			= ColumnRecognizer.computeScoredCandidates(headers, columnData);
		
		// Print scored candidates
		Iterator<ColumnConceptCandidate> it = scoredCandidates.iterator();
		while (it.hasNext()) {
			System.out.println(it.next().toString());
		}
	}
	
	private List<List<String>> getColumnsAsLists(RowTable rowTable) {
		List<Column> columns = rowTable.extractColumns();
		List<List<String>> strings = new ArrayList<List<String>>();
		Iterator<Column> it = columns.iterator();
		while (it.hasNext()) {
			Column column = it.next();
			strings.add(column.getContents());
		}
		
		return strings;
	}

	private List<ColumnConceptCandidate> recognizeTable(ColumnTable columnTable) {
		RowTable rowSample = columnTable.extractRowSample();
		
		// Create recognizers from specification file
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		File specificationFile = new File(SPECIFICATION_PATH);
//		ColumnRecognizerFactory.attachRecognizers(compositeCR, specificationFile, table, sample);
		ColumnRecognizerFactory.attachRecognizers(compositeCR, specificationFile, columnTable, rowSample);
		
		List<ColumnConceptCandidate> scoredCandidates = new ArrayList<ColumnConceptCandidate>();
		compositeCR.computeScoredCandidates(scoredCandidates);
		return scoredCandidates;
	}

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
		Column column = table.extractColumn(4);
		it = column.getFieldIterator();
//		while (it.hasNext()) {
		for (int i = 0; i < 4; i++) {
			System.out.println(it.next());
			System.out.println(LONG_DIVIDER);
		}
	}

	private void testWordFrequencies() {
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		Column column = table.extractColumn(6);
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
		Column restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		Column typeColumn = table.extractColumn(TYPE_COLUMN);
		Column idColumn = table.extractColumn(ID_COLUMN);
		Column addressColumn = table.extractColumn(ADDRESS_COLUMN);
		
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
		Column restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		Column typeColumn = table.extractColumn(TYPE_COLUMN);
		Column idColumn = table.extractColumn(ID_COLUMN);

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
				typeUniqueness,
				typeSimilarity));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Osterie Restaurant name", 
				restaurantUniqueness, 
				restaurantSimilarity));
		System.out.println(String.format(Locale.US, "-1 1:%f 2:%f # Osterie ID", 
				idUniqueness, 
				idSimilarity));
	}

	private TFIDFVector computeViviFiemmeRestaurantFeatures(InverseColumnFrequency inverseFrequencies) {
		final int ID_COLUMN = 1;
		final int TYPE_COLUMN = 2;
		final int RESTAURANT_NAME_COLUMN = 3;
		final String CSV_PATH = "Punti-di-ristoro-ViviFiemme.csv";
		
		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), ',');
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		Column restaurantColumn = table.extractColumn(RESTAURANT_NAME_COLUMN);
		Column typeColumn = table.extractColumn(TYPE_COLUMN);
		Column idColumn = table.extractColumn(ID_COLUMN);
		
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
				typeUniqueness, 
				typeSimilarity));
		System.out.println(String.format(Locale.US, "1 1:%f 2:%f # Vivifiemme Restaurant name", 
				restaurantUniqueness, 
				restaurantSimilarity));
		System.out.println(String.format(Locale.US, "-1 1:%f 2:%f # Vivifiemme ID", 
				idUniqueness, 
				idSimilarity));

		return restaurantVector;
	}
	
	private void testNLPPipeline() {
		
		ContextLoader cl = new ContextLoader();
		boolean contains = cl.getApplicationContext().containsBean("ODHPipeline");
		Object object = cl.getApplicationContext().getBean("ODHPipeline");
//		ODHPipeline<NLPParameters> headerPipeline = (ODHPipeline<NLPParameters>)cl.getApplicationContext().getBean("ODHPipeline");
//		ODHPipeline<NLPParameters> headerPipeline = cl.getApplicationContext().getBean(ODHPipeline.class);
		INLPPipeline<NLPParameters> headerPipeline = cl.getApplicationContext().getBean("ODHPipeline", INLPPipeline.class);
		NLPParameters parameters = cl.getApplicationContext().getBean(NLPParameters.class);
		IKnowledgeBaseService kbService = cl.getApplicationContext().getBean(IKnowledgeBaseService.class);
		KnowledgeBase kb = kbService.readKnowledgeBase("uk");
		
		List<Vocabulary> vocabularies = kb.getVocabularies();
//		int vocabularyCount = vocabularies.size();
		
		final String CSV_PATH = "Elenco_osterie_tipiche_civici.1386925759.csv";
		final char COLUMN_SEPARATOR = ';';

		RowTable table = RowTable.loadFromCSV(new File(CSV_PATH), COLUMN_SEPARATOR);
		String headerRow = table.getRowIterator().next();
//		List<String> headers = new ArrayList<String>(Arrays.asList(
//					CSVProcessor.splitRecord(headerRow, COLUMN_SEPARATOR)));
		List<String> headers = new ArrayList<String>();
		headers.add("dog");
		
		System.out.println(headers);
		
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		int columnNumber = 1;
		Iterator<String> itHeader = headers.iterator();
		while (itHeader.hasNext()) {
			String header = itHeader.next();
			NLText nlText = headerPipeline.runPipeline(header, kb, parameters);
			Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
			candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, "", meanings));
			columnNumber++;
		}

		System.out.println(candidates.toString());
	}
	
	private void testWebAPI() {


        IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH,
"ui.disi.unitn.it", 8092);
//        IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH,
//"opendata.disi.unitn.it", 8080);
		        PipelineClient pipelineClient = new PipelineClient(api);

		        List<String> text = new ArrayList<String>();
		        text.add("indirizzo");
		        text.add("comune");
		        text.add("descIt");

		        // Run a pipeline
		        //Map<String, String> params = new HashMap<String, String>();

		        NLPInput input = new NLPInput();
		        input.setText(text);
		        //input.setNlpParameters(params);

		        NLText[] result = pipelineClient.run("KeywordTextPipeline", input, 1l);
				List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
				int columnNumber = 1;
		        for (NLText nlText : result) {
		            System.out.println(nlText.toString());
					Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
					candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, "", meanings));
					columnNumber++;
		        }		
				System.out.println(candidates.toString());
	}
	
	private void testTypeDectection() {
		String dateString = "14/03/14";
		String intString = "12";
		String floatString = "3.14";
		String stringString = "hello world";
		String xmlString = "<hello>world</hello>";
//		Datatype type = DataTypeGuess.guessType(dateString);
		Datatype intType = DataTypeGuess.guessType(intString);
		Datatype floatType = DataTypeGuess.guessType(floatString);
//		Datatype dateType = DataTypeGuess.guessType(dateString);	
		boolean isXML = XmlTypeChecker.check(stringString);
		boolean isJson = JsonTypeChecker.check(stringString);
		boolean isEmpty = EmptyTypeChecker.check(stringString);
		boolean isList = ListTypeChecker.check(stringString);
		System.out.println("Type of " + intString + " is " + intType);
		System.out.println("Type of " + floatString + " is " + floatType);
		System.out.println("Is XML: " + isXML);
		System.out.println("Is JSON: " + isJson);
		System.out.println("Is empty: " + isEmpty);
		System.out.println("Is list: " + isList);
		
//		Datatype stringType = DataTypeGuess.guessType(stringString);
//		Datatype xmlType = DataTypeGuess.guessType(xmlString);
//		System.out.println("Type of " + stringString + " is " + stringType);
//		System.out.println("Type of " + xmlString + " is " + xmlType);
	}

/*
 * Superseded by unit test
 * 
	private void testFusionClassifier() {
		final String MODEL_FILE_PATH = "svm-model-minimal";
		final String INPUT_RECOGNIZER_ID = "it_restaurant_tf_idf";
		final long RESTAURANT_CONCEPT_ID = 2001;
		List<List<Double>> columnFeatures = new ArrayList<List<Double>>();
		List<String> inputRecognizers = new ArrayList<String>();
		List<Map<String, Double>> supportingCandidates = new ArrayList<Map<String, Double>>();
		List<Map<String, Double>> competingCandidates = new ArrayList<Map<String, Double>>();
		
		// Supporting candidates
		Map<String, Double> supportingCandidatesTipo = new HashMap<String, Double>();
		supportingCandidatesTipo.put(INPUT_RECOGNIZER_ID, 0.633739);
		supportingCandidates.add(supportingCandidatesTipo);

		Map<String, Double> supportingCandidatesInsegna = new HashMap<String, Double>();
		supportingCandidatesInsegna.put(INPUT_RECOGNIZER_ID, 0.285785);
		supportingCandidates.add(supportingCandidatesInsegna);
		
		// Competing candidates
		Map<String, Double> competingCandidatesTipo = new HashMap<String, Double>();
		competingCandidatesTipo.put(INPUT_RECOGNIZER_ID, 0.);
		competingCandidates.add(competingCandidatesTipo);
		Map<String, Double> competingCandidatesInsegna = new HashMap<String, Double>();
		competingCandidatesInsegna.put(INPUT_RECOGNIZER_ID, 0.);
		competingCandidates.add(competingCandidatesInsegna);

		// Two columns: insegna and tipo from osterie_tipiche
		// A single column feature: uniqueness
		List<Double> featuresTipo = new ArrayList<Double>();
		featuresTipo.add(0.145833);
		columnFeatures.add(featuresTipo);
		
		List<Double> featuresInsegna = new ArrayList<Double>();
		featuresInsegna.add(1.0);
		columnFeatures.add(featuresInsegna);
		
		// A single input recognizer
		inputRecognizers.add(INPUT_RECOGNIZER_ID);

		// Create the classifier
		FusionClassifier classifier 
			= new FusionClassifier(new File(MODEL_FILE_PATH), 
					columnFeatures, 
					RESTAURANT_CONCEPT_ID, 
					inputRecognizers);
		List<Double> predictions 
			= classifier.classifyColumns(supportingCandidates, competingCandidates);
		
		// Print predictions
		System.out.println(predictions.toString());
	}
*/
}
