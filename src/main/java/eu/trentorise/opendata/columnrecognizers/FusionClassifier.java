package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The FusionClassifier provides an interface to the SVM classifier used for 
 * fusing column recognition results.
 * 
 * @author Simon
 *
 */
public class FusionClassifier {
	/**
	 * Where to put the file of examples to classify
	 */
//	public final static String EXAMPLE_FILE_PATH = "svm-examples.txt";
	public final static String EXAMPLE_FILE_NAME = "svm-examples.txt";
	
	/**
	 * Where to put the predictions produced by the classifier
	 */
//	public final static String PREDICTIONS_FILE_PATH = "svm-predictions.txt";
	public final static String PREDICTIONS_FILE_NAME = "svm-predictions.txt";
	
	/**
	 * Path to the classifier executable
	 */
//	public final static String CLASSIFICATION_EXE_PATH = "../svm-light/svm_classify.exe";
	public final static String CLASSIFIER_EXE_NAME = "svm_classify.exe";
	
	/**
	 * The SVM-Light model file
	 */
	File modelFile = null;
	
	/**
	 * The data table column features
	 */
	List<List<Double>> columnFeatures = null;
	
	/**
	 * The target concept ID 
	 */
	long conceptID = -1;
	
	/**
	 * The recognizers the output of which we will fuse
	 */
	List<String> inputRecognizers = null;
	
	/**
	 * Constructs the classifier.
	 * 
	 * @param modelFile			The SVM-Light model file
	 * @param columnFeatures	The data table column features
	 * @param conceptID			The target concept ID
	 * @param inputRecognizers	The IDs of the recognizers to fuse
	 */
	public FusionClassifier(File modelFile, 
			List<List<Double>> columnFeatures,
			long conceptID,
			List<String> inputRecognizers) {
		super();
		this.modelFile = modelFile;
		this.columnFeatures = columnFeatures;
		this.conceptID = conceptID;
		this.inputRecognizers = inputRecognizers;
	}

	/**
	 * Performs classification on all the table columns.
	 * Supporting and competing candidates are passed as List (one element per
	 * column) of Map from recognizer ID to score.
	 * 
	 * @param supportingCandidates	The supporting candidates for each column
	 * @param competingCandidates	The competing candidates for each column
	 * @return						The prediction for each column
	 */
	public List<Double> classifyColumns(List<Map<String, Double>> supportingCandidates,
			List<Map<String, Double>> competingCandidates) {
		List<List<Double>> featureVectors 
			= buildFeatureVectors(supportingCandidates, competingCandidates);
		writeExamples(featureVectors);
		classify();
		return readPredictions();
	}

	/**
	 * Writes out the unlabeled examples (the columns) in the format readable 
	 * by SVM-Light. The labels are set to a default value.
	 * 
	 * @param featureVectors		A feature vector for each column
	 */
	public static void writeExamples(List<List<Double>> featureVectors) {
		File exampleFile = new File(FileUtils.getDataFolder(), EXAMPLE_FILE_NAME);
//		SVMLightWriter writer = new SVMLightWriter(new File(EXAMPLE_FILE_PATH), featureVectors);
		SVMLightWriter writer = new SVMLightWriter(exampleFile, featureVectors);
		writer.write();
	}

	/**
	 * Writes out the examples (the columns) and the labels in the format 
	 * readable by SVM-Light.
	 * 
	 * @param featureVectors		A feature vector for each column
	 * @param labels				A label for each column
	 */
	public static void writeExamples(List<List<Double>> featureVectors,
			List<Double> labels) {
		File exampleFile = new File(FileUtils.getDataFolder(), EXAMPLE_FILE_NAME);
//		SVMLightWriter writer 
//		= new SVMLightWriter(new File(EXAMPLE_FILE_PATH), featureVectors, labels);
		SVMLightWriter writer 
			= new SVMLightWriter(exampleFile, featureVectors, labels);
		writer.write();
	}

	/**
	 * Builds the feature vectors for all the columns of a table.
	 * 
	 * @param supportingCandidates	The supporting candidates
	 * @param competingCandidates	The competing candidates
	 * @return
	 */
	public List<List<Double>> buildFeatureVectors(
			List<Map<String, Double>> supportingCandidates,
			List<Map<String, Double>> competingCandidates) {
		Iterator<List<Double>> itColumnFeatures = columnFeatures.iterator();
		Iterator<Map<String, Double>> itSupportingCandidates = supportingCandidates.iterator();
		Iterator<Map<String, Double>> itCompetingCandidates = competingCandidates.iterator();
		
		List<List<Double>> featureVectors = new ArrayList<List<Double>>();
		while (itColumnFeatures.hasNext()) {
			assert(itSupportingCandidates.hasNext());
			assert(itCompetingCandidates.hasNext());
			List<Double> featureVector = buildFeatureVector(itColumnFeatures.next(),
					itSupportingCandidates.next(),
					itCompetingCandidates.next());
			featureVectors.add(featureVector);
		}
		return featureVectors;
	}

	/**
	 * Builds the feature vector that describes a column.
	 * The first elements of the vector are the column features (for example
	 * uniqueness, data types). The remaining elements are two for each input
	 * recognizer: one for the candidate (if any) supporting the hypothesis
	 * that the column represents the target concept ID, one for the 
	 * highest-scoring candidate representing an alternative concept ID.
	 * 
	 * @param features			Column features for the column
	 * @param supporters		Supporting candidates for the column
	 * @param competitors		Competing candidates for the column
	 * @return					The feature vector
	 */
	private List<Double> buildFeatureVector(List<Double> features,
			Map<String, Double> supporters,
			Map<String, Double> competitors) {
		List<Double> featureVector = new ArrayList<Double>();
		Iterator<Double> itFeatures = features.iterator();
		while (itFeatures.hasNext()) {
			featureVector.add(itFeatures.next());
		}
		Iterator<String> itRecognizers = inputRecognizers.iterator();
		while (itRecognizers.hasNext()) {
			String recognizerID = itRecognizers.next();
			featureVector.add(supporters.get(recognizerID));
			featureVector.add(competitors.get(recognizerID));
		}
		return featureVector;
	}

	/**
	 * Classifies the examples by running SVM-Light.
	 * 
	 */
	private void classify() {
		File classifierExe = new File(FileUtils.getSVMExecutablesFolder(), CLASSIFIER_EXE_NAME);
		File exampleFile = new File(FileUtils.getDataFolder(), EXAMPLE_FILE_NAME);
		File predictionsFile = new File(FileUtils.getDataFolder(), PREDICTIONS_FILE_NAME);
//		String[] commandArray = {
//				CLASSIFICATION_EXE_PATH,
//				EXAMPLE_FILE_PATH,
//				modelFile.getAbsolutePath(),
//				PREDICTIONS_FILE_PATH
//		};
		String[] commandArray = {
				classifierExe.getAbsolutePath(),
				exampleFile.getAbsolutePath(),
				modelFile.getAbsolutePath(),
				predictionsFile.getAbsolutePath()
		};
		ProcessLauncher.run(commandArray);
	}

	/**
	 * Loads the predictions from the SVM-Light output file.
	 * 
	 * @return		The prediction vector
	 */
	private List<Double> readPredictions() {
	// TODO use static read method
		File predictionsFile = new File(FileUtils.getDataFolder(), PREDICTIONS_FILE_NAME);
//		VectorReader reader = new VectorReader(new File(PREDICTIONS_FILE_PATH));
		VectorReader reader = new VectorReader(predictionsFile);
		reader.read();
		return reader.getVector();
	}
	
}
