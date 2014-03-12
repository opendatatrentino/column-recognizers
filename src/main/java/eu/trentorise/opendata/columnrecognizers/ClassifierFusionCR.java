package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * The ClassifierFusionCR uses the SVM classifier to fuse the inputs of other
 * column recognizers.
 * 
 * @author Simon
 *
 */
public class ClassifierFusionCR extends FusionColumnRecognizer {
	/**
	 * The SVM classifier
	 */
	private FusionClassifier classifier = null;
	
	/**
	 * The number of columns in the table
	 */
	private int columnCount = 0;
	
	/**
	 * The IDs of the recognizers that provide the candidates to be fused
	 */
	List<String> inputRecognizers = null;
	
	/**
	 * Constructs the ClassifierFusionCR.
	 * 
	 * @param id				The identifier for this recognizer instance
	 * @param conceptID			The concept ID
	 * @param table				The data table
	 * @param modelFile			The SVM model file
	 * @param inputRecognizers	The recognizers that provide input candidates
	 */
	public ClassifierFusionCR(String id, 
			long conceptID, 
			Table table,
			File modelFile, 
			List<String> inputRecognizers) {
		super(id, conceptID);
		columnCount = table.getColumnCount();
		List<List<Double>> columnFeatures = table.getColumnFeatures(); 
		this.inputRecognizers = inputRecognizers;
		classifier = new FusionClassifier(modelFile, columnFeatures, conceptID, inputRecognizers);
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.ColumnRecognizer#computeScoredCandidates(java.util.List)
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		List<Map<String, Double>> supportingCandidates = makeNewCandidateMapList();
		List<Map<String, Double>> competingCandidates = makeNewCandidateMapList();
		
		// TODO use candidatesToMapLists
		ListIterator<ColumnConceptCandidate> itCandidate = candidates.listIterator();
		while (itCandidate.hasNext()) {
			ColumnConceptCandidate candidate = itCandidate.next();
			boolean isSupporting = getConceptID() == candidate.getConceptID();
			int columnNumber = candidate.getColumnNumber();
			String originator = candidate.getOriginator();
			double score = candidate.getScore();
			
			if (isSupporting) {
				supportingCandidates.get(columnNumber - 1).put(originator, score);
			} else {
				Map<String, Double> map = competingCandidates.get(columnNumber - 1);
				double currentScore = map.get(originator);
				if (candidate.getScore() > currentScore) {
					map.put(originator, candidate.getScore());
				}
			}
			
			updateCandidates(itCandidate, candidate);
		}
		
		List<Double> predictions 
			= classifier.classifyColumns(supportingCandidates, competingCandidates);
		createCandidatesFromPredictions(predictions, candidates);
	}

	/**
	 * Creates and adds new column-concept candidates from the predictions 
	 * output by the classifier.
	 * 
	 * @param predictions	Classifier predictions
	 * @param candidates	The candidate list
	 */
	private void createCandidatesFromPredictions(List<Double> predictions,
			List<ColumnConceptCandidate> candidates) {
		Iterator<Double> itPrediction = predictions.iterator();
		int columnNumber = 1;
		while (itPrediction.hasNext()) {
			double prediction = itPrediction.next();
			double score = predictionToScore(prediction);
			if (score > 0) {
				candidates.add(new ColumnConceptCandidate(columnNumber, 
						getConceptID(), 
						score, 
						getId()));
			}
			columnNumber++;
		}
	}

	/**
	 * Creates a new list of candidate maps.
	 * 
	 * @return		The new list
	 */
	private List<Map<String, Double>> makeNewCandidateMapList() {
		List<Map<String, Double>> candidateMapList = new ArrayList<Map<String, Double>>();
		for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
			candidateMapList.add(makeNewCandidateMap());
		}
		return candidateMapList;
	}

	/**
	 * Creates a new map of input recognizer ID to score.
	 * 
	 * @return	The new map
	 */
	private Map<String, Double> makeNewCandidateMap() {
		Map<String, Double> candidateMap = new HashMap<String, Double>();
		Iterator<String> itRecognizer = inputRecognizers.iterator();
		while (itRecognizer.hasNext()) {
			String recognizerID = itRecognizer.next();
			candidateMap.put(recognizerID, 0.0);
		}
		return candidateMap;
	}

	/**
	 * Turn a classifier prediction into a candidate score
	 * Classifier predictions (typically) are in the range [-1, 1]. 
	 * For now, we turn them into scores by just truncating the range to [0, 1].
	 * 
	 * @param prediction	The classifier output
	 * @return				The candidate score
	 */
	private double predictionToScore(double prediction) {
		return Math.min(Math.max(prediction, 0.0), 1.0);
	}

	
	/**
	 * Trains the classifier on the contents of tables + label files 
	 * + classifier output.
	 * 
	 * Argument format:
	 * 
	 * 	<fusion-recognizer-id> <cr-specification-file.txt> <table.csv> 
	 *  <column-separator-char> <label-file.txt> ...
	 * 
	 * The specification file specifies the input recognizers as well as the 
	 * fusion recognizer. Each label file provides labels for each of the 
	 * columns in the corresponding table (1 for positive examples, -1 for 
	 * negative ones).
	 * 
	 * By convention, files are grouped in directories like this:
	 * 
	 *   svm-<cr_name>
	 *     recognizers.txt
	 *     svm-labels-<cr_name>-<table_name>.txt
	 *     svm-examples-<cr_name>.txt
	 *     svm-model-<cr_name>
	 *     
	 * The training examples file contains labeled examples for all the tables. 
	 * The output SVM model is named svm-model-<cr_name>. 
	 * 
	 * @param args	The arguments
	 */
	public static void main(String[] args) {
		final int FUSION_RECOGNIZER_ID_POSITION = 0;
		
		checkParameters(args);
		
		String fusionRecognizerID = args[FUSION_RECOGNIZER_ID_POSITION];
		List<File> tableFiles = new ArrayList<File>();
		List<Character> columnSeparators = new ArrayList<Character>();
		List<File> labelFiles = new ArrayList<File>();
		getTrainingFilesFromArgs(args, tableFiles, columnSeparators, labelFiles);
		
		train(fusionRecognizerID, tableFiles, columnSeparators, labelFiles);
	}

	/**
	 * Performs classifier training on multiple table files (or only one).
	 * 
	 * @param fusionRecognizerID	The name of the fusion recognizer
	 * @param tableFiles			The CSV tables
	 * @param columnSeparators		Column separators for CSV tables
	 * @param labelFiles			Training labels for each table
	 */
	private static void train(
			String fusionRecognizerID,
			List<File> tableFiles,
			List<Character> columnSeparators,
			List<File> labelFiles) {
		List<List<Double>> allExamples = new ArrayList<List<Double>>();
		List<Double> allLabels = new ArrayList<Double>();		
		Iterator<File> itTables = tableFiles.iterator();
		Iterator<Character> itColumnSeparators = columnSeparators.iterator();
		Iterator<File> itLabels = labelFiles.iterator();
		File specificationFile = FileUtils.getSVMTrainingCRSpecificationFile(fusionRecognizerID);
		
		while (itTables.hasNext()) {
			produceTrainingExamples(fusionRecognizerID, 
					specificationFile,
					itTables.next(), 
					itColumnSeparators.next(),
					itLabels.next(), 
					allExamples, 
					allLabels);
		}

		File exampleFile = FileUtils.getSVMTrainingFile(fusionRecognizerID);
		File modelFile = FileUtils.getSVMModelFile(fusionRecognizerID);
		FusionClassifier.writeExamples(exampleFile, allExamples, allLabels);
		FusionClassifier.train(exampleFile, modelFile);
	}

	/**
	 * Runs the input recognizers and puts together the SVM training examples
	 * for a given table (CSV file). The examples (feature lists) are added to 
	 * allExamples. The labels (floating point numbers from {-1, 1}) are added
	 * to allLabels.
	 * 
	 * @param fusionRecognizerID	The name of the fusion recognizer
	 * @param specificationFile		Specifying the input CRs and the fusion CRs
	 * @param tableFile				The training table file
	 * @param labelFile				Training labels, one of {-1, 1} for each column
	 * @param allExamples			The example list
	 * @param allLabels				The list of training labels
	 */
	private static void produceTrainingExamples(
			String fusionRecognizerID,
			File specificationFile,
			File tableFile, 
			char columnSeparator,
			File labelFile,
			List<List<Double>> allExamples, 
			List<Double> allLabels) {
		assert(allExamples != null);
		assert(allLabels != null);
		
		RowTable table = RowTable.loadFromCSV(tableFile, columnSeparator);
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		ColumnRecognizerFactory.attachRecognizers(compositeCR, 
				specificationFile, 
				table, 
				table.extractRowSample());
		
		ClassifierFusionCR fusionCR = (ClassifierFusionCR)compositeCR.detach(fusionRecognizerID);
		
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		compositeCR.computeScoredCandidates(candidates);

		List<Map<String, Double>> supportingCandidates = fusionCR.makeNewCandidateMapList();
		List<Map<String, Double>> competingCandidates = fusionCR.makeNewCandidateMapList();
		fusionCR.candidatesToMapLists(candidates, supportingCandidates, competingCandidates);
		
		fusionCR.produceTrainingExamples(supportingCandidates, 
				competingCandidates, 
				labelFile,
				allExamples, 
				allLabels);
	}

	/**
	 * Produces SVM training examples.
	 * 
	 * @param supportingCandidates	The supporting candidates
	 * @param competingCandidates	The competing candidates
	 * @param labelFile				File containing training labels
	 * @param allExamples			The list that collects the examples
	 * @param allLabels				The list that collects the labels
	 */
	private void produceTrainingExamples(
			List<Map<String, Double>> supportingCandidates,
			List<Map<String, Double>> competingCandidates,
			File labelFile, 
			List<List<Double>> allExamples, 
			List<Double> allLabels) {
		List<List<Double>> examples 
			= classifier.buildFeatureVectors(supportingCandidates, competingCandidates);
		allExamples.addAll(examples);
		
		List<Double> labels = VectorReader.read(labelFile);
		allLabels.addAll(labels);
	}

	/**
	 * Produces maps of supporting and maximum competing candidate scores for
	 * column. The candidate list is updated in the process so that
	 * the "used" input candidates are removed. 
	 * <p>
	 * Each "map list" contains a map for each table column. Each map is from
	 * recognizer identifier to score. The score is either the supporting score
	 * (in supportingCandidates) or the maximum competing score 
	 * (competingCandidates).
	 * 
	 * @param candidates				The candidate list
	 * @param supportingCandidates		Supporting candidate map list
	 * @param competingCandidates		Competing candidate map list
	 */
	private void candidatesToMapLists(
			List<ColumnConceptCandidate> candidates,
			List<Map<String, Double>> supportingCandidates,
			List<Map<String, Double>> competingCandidates) {
		ListIterator<ColumnConceptCandidate> itCandidate = candidates.listIterator();
		while (itCandidate.hasNext()) {
			ColumnConceptCandidate candidate = itCandidate.next();
			boolean isSupporting = getConceptID() == candidate.getConceptID();
			int columnNumber = candidate.getColumnNumber();
			String originator = candidate.getOriginator();
			double score = candidate.getScore();
			
			if (isSupporting) {
				supportingCandidates.get(columnNumber - 1).put(originator, score);
			} else {
				Map<String, Double> map = competingCandidates.get(columnNumber - 1);
				double currentScore = map.get(originator);
				if (candidate.getScore() > currentScore) {
					map.put(originator, candidate.getScore());
				}
			}
			
			updateCandidates(itCandidate, candidate);
		}		
	}

	/**
	 * Verifies the arguments of the main method.
	 * 
	 * @param args	The arguments
	 */
	private static void checkParameters(String[] args) {
		final int FILE_ARGUMENT_GROUP_SIZE = 3;
		final int MINIMAL_ARG_COUNT = 1 + FILE_ARGUMENT_GROUP_SIZE;
		
		int argCount = args.length;
		if (argCount < MINIMAL_ARG_COUNT) {
			throw new RuntimeException("Not enough parameters");			
		}
		int optionalArgCount = argCount - MINIMAL_ARG_COUNT;
		if (optionalArgCount % FILE_ARGUMENT_GROUP_SIZE != 0) {
			throw new RuntimeException("Wrong number of parameters");
		}
	}

	/**
	 * Gets the file paths from the argument list and turns them into File
	 * objects. 
	 * 
	 * @param args				The arguments
	 * @param tableFiles		The table (CSV) files 
	 * @param columnSeparators 	The column separator for each CSV file
	 * @param labelFiles		The label files for supervised training
	 */
	private static void getTrainingFilesFromArgs(String[] args,
			List<File> tableFiles,
			List<Character> columnSeparators, 
			List<File> labelFiles) {
		final int TRAINING_FILES_START_POSITION = 1;
		
		int argIndex = TRAINING_FILES_START_POSITION;
		while (argIndex < args.length) {
			tableFiles.add(new File(args[argIndex++]));
			columnSeparators.add(args[argIndex++].charAt(0));
			labelFiles.add(new File(args[argIndex++]));
		}
	}
	
}
