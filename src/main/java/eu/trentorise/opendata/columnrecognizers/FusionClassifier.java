package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public final static String EXAMPLE_FILE_NAME = "svm-examples.txt";
	
	/**
	 * Where to put the predictions produced by the classifier
	 */
	public final static String PREDICTIONS_FILE_NAME = "svm-predictions.txt";
	
	/**
	 * Path to the classifier executable
	 */
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
	Set<String> inputRecognizers = null;
	
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
			Set<String> inputRecognizers) {
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
	 * Produce a model file from examples by running SVM-Light.
	 * 
	 * @param exampleFile		The training examples
	 * @param modelFile			The output file
	 */
	public static void train(File exampleFile, File modelFile) {
		/* 
		 * Learning options for SVM-Light
		 * 
	        -j float    - Cost: cost-factor, by which training errors on
	        positive examples outweight errors on negative
	        examples (default 1) (see [Morik et al., 1999])
	        
	     *
	     * We weight positive example heavily because we
	     * 	- typically have few of them
	     *  - prefer false positives to false negatives
	     *  
	     * In one experiment (identifying the restaurant name column in the 
	     * Osterie table), a cost factor of 5 was just enough to generate a 
	     * score close to one for the restaurant name column.
        */
		final String COST_FACTOR_OPTION = "-j";
		final String COST_FACTOR = "10";
        
		String[] commandArray = {
				FileUtils.getSVMLearner().getAbsolutePath(),
				COST_FACTOR_OPTION,
				COST_FACTOR,
				exampleFile.getAbsolutePath(),
				modelFile.getAbsolutePath()
		};
		int exitValue = ProcessLauncher.run(commandArray);
		if (exitValue != 0) {
			throw new RuntimeException("Failure running SVM-Light learner");
		}
	}

	/**
	 * Writes out the unlabeled examples (the columns) in the format readable 
	 * by SVM-Light. The labels are set to a default value.
	 * 
	 * @param featureVectors		A feature vector for each column
	 */
	public static void writeExamples(List<List<Double>> featureVectors) {
//		File exampleFile = new File(FileUtils.getDataFolder(), EXAMPLE_FILE_NAME);
		File exampleFile = FileUtils.getTmpFile(EXAMPLE_FILE_NAME);
		SVMLightWriter writer = new SVMLightWriter(exampleFile, featureVectors);
		writer.write();
	}

	/**
	 * Writes out the examples (the columns) and the labels in the format 
	 * readable by SVM-Light.
	 * 
	 * @param exampleFile			The output file
	 * @param featureVectors		A feature vector for each column
	 * @param labels				A label for each column
	 */
	public static void writeExamples(File exampleFile,
			List<List<Double>> featureVectors,
			List<Double> labels) {
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
		File classifierExe = FileUtils.getSVMClassifier();
		File exampleFile = FileUtils.getTmpFile(EXAMPLE_FILE_NAME);
		File predictionsFile = FileUtils.getTmpFile(PREDICTIONS_FILE_NAME);
		String[] commandArray = {
				classifierExe.getAbsolutePath(),
				exampleFile.getAbsolutePath(),
				modelFile.getAbsolutePath(),
				predictionsFile.getAbsolutePath()
		};
		int exitValue = ProcessLauncher.run(commandArray);
		if (exitValue != 0) {
			throw new RuntimeException("Failure running SVM-Light classifier");
		}
	}

	/**
	 * Loads the predictions from the SVM-Light output file.
	 * 
	 * @return		The prediction vector
	 */
	private List<Double> readPredictions() {
	// TODO use static read method
		File predictionsFile = FileUtils.getTmpFile(PREDICTIONS_FILE_NAME);
		VectorReader reader = new VectorReader(predictionsFile);
		reader.read();
		return reader.getVector();
	}
	
}
