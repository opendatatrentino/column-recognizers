package eu.trentorise.opendata.columnrecognizers;

import static org.junit.Assert.*;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

/**
 * @author Simon
 *
 */
public class ClassifierFusionTest {
	private static final String MINIMAL_FUSION_CR_NAME = "minimal_fusion";
	private static final String TWO_FEATURES_OSTERIE_CR_NAME = "two_features_osterie";
	private static final String INPUT_RECOGNIZER_ID = "it_restaurant_tf_idf";
	private static final long RESTAURANT_CONCEPT_ID = 2001;
	private static final double TIPO_SIMILARITY_SCORE = 0.633739;
	private static final double INSEGNA_SIMILARITY_SCORE = 0.285785;
	

	/**
	 * Tests the classifier with the simple model of two features: uniqueness 
	 * and restaurant-name-similarity.
	 * 
	 * The model is trained and tested on two columns: 'tipo' and 'insegna' 
	 * from osterie tipiche.
	 * 
	 */
	@Test
	public void testClassifier() {
		// Ensure there are no intermediate files left in tmp directory
		File exampleFile = FileUtils.getTmpFile(FusionClassifier.EXAMPLE_FILE_NAME);
		File predictionsFile = FileUtils.getTmpFile(FusionClassifier.PREDICTIONS_FILE_NAME);
		exampleFile.delete();
		predictionsFile.delete();
		
		List<List<Double>> columnFeatures = new ArrayList<>();
		Set<String> inputRecognizers = new HashSet();
		List<Map<String, Double>> supportingCandidates = new ArrayList();
		List<Map<String, Double>> competingCandidates = new ArrayList();
		
		// Supporting candidates
		Map<String, Double> supportingCandidatesTipo = new HashMap();
		supportingCandidatesTipo.put(INPUT_RECOGNIZER_ID, TIPO_SIMILARITY_SCORE);
		supportingCandidates.add(supportingCandidatesTipo);

		Map<String, Double> supportingCandidatesInsegna = new HashMap();
		supportingCandidatesInsegna.put(INPUT_RECOGNIZER_ID, INSEGNA_SIMILARITY_SCORE);
		supportingCandidates.add(supportingCandidatesInsegna);
		
		// Competing candidates
		Map<String, Double> competingCandidatesTipo = new HashMap();
		competingCandidatesTipo.put(INPUT_RECOGNIZER_ID, 0.);
		competingCandidates.add(competingCandidatesTipo);
		Map<String, Double> competingCandidatesInsegna = new HashMap();
		competingCandidatesInsegna.put(INPUT_RECOGNIZER_ID, 0.);
		competingCandidates.add(competingCandidatesInsegna);

		// Two columns: insegna and tipo from osterie_tipiche
		// A single column feature: uniqueness
		List<Double> featuresTipo = new ArrayList();
		featuresTipo.add(0.145833);
		columnFeatures.add(featuresTipo);
		
		List<Double> featuresInsegna = new ArrayList();
		featuresInsegna.add(1.0);
		columnFeatures.add(featuresInsegna);
		
		// A single input recognizer
		inputRecognizers.add(INPUT_RECOGNIZER_ID);

		// Create the classifier
		FusionClassifier classifier 
			= new FusionClassifier(FileUtils.getSVMModelFile(MINIMAL_FUSION_CR_NAME), 
					columnFeatures, 
					RESTAURANT_CONCEPT_ID, 
					inputRecognizers);
		List<Double> predictions 
			= classifier.classifyColumns(supportingCandidates, competingCandidates);
		
		boolean tipoIsNegativeExample = predictions.get(0) < -0.5;
		
//		TODO This currently doesn't work -- need to investigate
//		boolean insegnaIsPositiveExample = predictions.get(1) > 0.5;
		
		assertTrue(tipoIsNegativeExample);
//		assertTrue(insegnaIsPositiveExample);
		
	try {
		System.out.println(new File(".").getCanonicalPath());
		System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		
	}

	/**
	 * Tests the classifier fusion recognizer with the simple model of two 
	 * features: uniqueness and restaurant-name-similarity.
	 * 
	 * The model is trained and tested on all the columns of osterie tipiche.
	 * 
	 */
//	@Test
	public void testRecognizer() {
		final int TIPO_COLUMN_NUMBER = 4;
		final int INSEGNA_COLUMN_NUMBER = 3;
		final String FUSION_CR_ID = "fusion";
		final String CSV_PATH = "/tables/Elenco_osterie_tipiche_civici.1386925759.csv";
		final char COLUMN_SEPARATOR = ';';

		// Load CSV file
//		File csvFile = new File(CSV_PATH); 
		File csvFile = FileUtils.getResourceFile(CSV_PATH);
		RowTable rowTable = RowTable.loadFromCSV(csvFile, COLUMN_SEPARATOR);
		rowTable.removeHeaders();
		
		// Create recognizer
		Set<String> inputRecognizers = new HashSet<String>();
		inputRecognizers.add(INPUT_RECOGNIZER_ID);
		File modelFile = FileUtils.getSVMModelFile(TWO_FEATURES_OSTERIE_CR_NAME);
		ClassifierFusionCR fusionCR 
			= new ClassifierFusionCR(FUSION_CR_ID, 
					RESTAURANT_CONCEPT_ID, 
					rowTable, 
					modelFile, 
					inputRecognizers);
		
		// Make list of candidates
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		ColumnConceptCandidate tipoCandidate
			= new ColumnConceptCandidate(TIPO_COLUMN_NUMBER, 
					RESTAURANT_CONCEPT_ID, 
					TIPO_SIMILARITY_SCORE,
					INPUT_RECOGNIZER_ID);
		candidates.add(tipoCandidate);
		ColumnConceptCandidate insegnaCandidate
			= new ColumnConceptCandidate(INSEGNA_COLUMN_NUMBER, 
				RESTAURANT_CONCEPT_ID, 
				INSEGNA_SIMILARITY_SCORE,
				INPUT_RECOGNIZER_ID);
		candidates.add(insegnaCandidate);
		
		// Run recognizer
		fusionCR.computeScoredCandidates(candidates);
		
		// Make sure input candidates are gone...
		assertTrue(candidates.size() == 1);
		// ... and replaced with a new fusion candidate
		ColumnConceptCandidate resultingCandidate = candidates.get(0);
		assertTrue(resultingCandidate.getOriginator().equals(FUSION_CR_ID));
		
		// Verify the score of the fusion candidate
		assertTrue(resultingCandidate.getScore() > 0.7);
	}
	
//	@Test
	public void testTraining() {
//		TODO 
		String referenceModel = "";
		String model = "";
		
		assertTrue(model.equals(referenceModel));
	}
	

	
}
