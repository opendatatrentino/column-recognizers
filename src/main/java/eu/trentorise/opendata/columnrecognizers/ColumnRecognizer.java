package eu.trentorise.opendata.columnrecognizers;
import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLSenseMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLText;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * ColumnRecognizer is the abstract superclass for column recognizers.
 * 
 * @author Simon
 *
 */
public abstract class ColumnRecognizer {
	/** 
	 * A unique name identifying this recognizer
	 */
	private String id = null;
	
	/**
	 * Constructs the column recognizer.
	 * 	
	 * @param id	A unique name for the recognizer instance
	 */
	public ColumnRecognizer(String id) {
		super();
		this.id = id;
	}

	/**
	 * Computes the list of scored candidates and updates the candidate list.
	 * 
	 * @param candidates	The scored column-concept candidates
	 */
	public abstract void computeScoredCandidates(List<ColumnConceptCandidate> candidates);

	/**
	 * Gets the name (identifier) of this recognizer.
	 * 
	 * @return 	The unique name identifying the recognizer instance
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Convenience method for retrieving the best-scoring concept ID for each
	 * column, with -1 returned for columns with no candidate concepts.
	 * 
	 * @param columnHeaders	The column headers
	 * @param columnData	The column contents
	 * @return				The concept ID for each column
	 */
	public static List<Long> computeColumnConceptIDs(
		    List<String> columnHeaders,
		    List<List<String>> columnData) {
		int columnCount = columnData.size();
		List<ColumnConceptCandidate> candidates
			= computeScoredCandidates(columnHeaders, columnData);
		
		List<ColumnConceptCandidate> maxCandidates 
			= new ArrayList<ColumnConceptCandidate>(columnCount);
		for (int index = 0; index < columnCount; index++) {
			maxCandidates.add(null);
		}
		
		for (ColumnConceptCandidate candidate : candidates) {
			int columnIndex = candidate.getColumnNumber() - 1;
			ColumnConceptCandidate maxCandidate = maxCandidates.get(columnIndex);
			if (maxCandidate == null) {
				maxCandidate = candidate;
			} else {
				maxCandidate = OneBestFusionCR.pickMaxCandidate(maxCandidate, candidate);
			}

			maxCandidates.set(columnIndex, maxCandidate);
		}
		
		List<Long> maxIDs = new ArrayList<Long>(columnCount);
		for (ColumnConceptCandidate maxCandidate : maxCandidates) {
			if (maxCandidate == null) {
				maxIDs.add(-1L);
			} else {
				maxIDs.add(maxCandidate.getConceptID());
			}
		}
		
		return maxIDs;
	}
	
	/**
	 * Static API method for computing column-concept candidates for a table 
	 * using the default specification file.
	 * 
	 * @param columnHeaders	The column headers
	 * @param columnData	The column contents
	 * @return				The column-concept candidates
	 */
	public static List<ColumnConceptCandidate> computeScoredCandidates(
		    List<String> columnHeaders,
		    List<List<String>> columnData) {
		return computeScoredCandidates(
				columnHeaders, 
				columnData, 
				FileUtils.getDefaultSpecificationFile());
	}

	/**
	 * Static API method for computing column-concept candidates for a table.
	 * 
	 * @param columnHeaders		The column headers
	 * @param columnData		The column contents
	 * @param specificationFile	The specification file
	 * @return					The column-concept candidates
	 */
	public static List<ColumnConceptCandidate> computeScoredCandidates(
		    List<String> columnHeaders,
		    List<List<String>> columnData,
//		    File specificationFile) {
		    InputStream specificationFile) {
		return computeScoredCandidates(
				columnHeaders, 
				columnData,
				specificationFile,
				/* model directories: */ null);
	}

	/**
	 * Static API method for computing column-concept candidates allowing the
	 * caller to provide a set of directories for finding recognizer models.
	 * 
	 * @param columnHeaders		The column headers
	 * @param columnData		The column contents
	 * @param specificationFile	The specification file
	 * @param modelDirectories	A list of directories containing model files
	 * @return					The column-concept candidates
	 */
	public static List<ColumnConceptCandidate> computeScoredCandidates(
		    List<String> columnHeaders,
		    List<List<String>> columnData,
//			File specificationFile, 
			InputStream specificationFile, 
			List<File> modelDirectories) {
		ColumnTable columnTable = ColumnTable.makeColumnTableFromStringLists(columnHeaders, columnData);
		RowTable rowSample = columnTable.extractRowSample();
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		ColumnRecognizerFactory.attachRecognizers(
				compositeCR, 
				specificationFile, 
				modelDirectories,
				columnTable, 
				rowSample);
		compositeCR.computeScoredCandidates(candidates);
		
		return candidates;
	}
	
	/**
	 * Run the NLP pipeline on a text and extract the highest-probability concept ID
	 * 
	 * @param text		The text
	 * @return			
	 */
	public static long conceptFromText(String text) {
		long conceptID = -1;
		List<String> texts = new ArrayList<String>();
		texts.add(text);
		List<NLText> nlTexts = NLPUtils.processTexts(texts);
//System.out.println(nlTexts.get(0));
		Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlTexts.get(0));
//System.out.println(meanings.size());
		NLMeaning maxMeaning = NLPUtils.findMaxProbabilityMeaning(meanings);
		if (maxMeaning instanceof NLSenseMeaning) {
			conceptID = ((NLSenseMeaning)maxMeaning).getGlobalId();
		}
		
		return conceptID;
	}
	
	/**
	 * The main method is used for ad hoc testing.
	 * 
	 * @param args	Arguments - column names
	 */
	public static void main(String[] args) {
		testConceptFromText(args);
//		testComputeScoredCandidates(args);
	}

	private static void testConceptFromText(String[] args) {
		String columnName = args[0];
		System.out.println(conceptFromText(columnName));
	}

	private static void testComputeScoredCandidates(String[] args) {
	    List<String> columnHeaders = new ArrayList<String>();
	    for (int index = 0; index < args.length; index++) {
			columnHeaders.add(args[index]);	
	    }
	    List<List<String>> columnData = new ArrayList<List<String>>();
	    List<String> column = new ArrayList<String>();
	    for (int index = 0; index < args.length; index++) {
	    	column.add("");
	    }
	    columnData.add(column);

	    List<ColumnConceptCandidate> candidates 
	    	= computeScoredCandidates(columnHeaders, columnData);
		System.out.println(candidates);
		System.out.println(candidates.size());
	}

//	/**
//	 * Sets the folder with the SVM-Light executables. Use this if you want
//	 * to put the folder in a location different from the default.
//	 * 
//	 * @param svmExecutablesFolder The folder with the SVM-Light executables
//	 */
//	public static void setSVMExecutablesFolder(File svmExecutablesFolder) {
//		FileUtils.setSVMExecutablesFolder(svmExecutablesFolder);
//	}
//
//	/**
//	 * Sets the folder containing column recognizer data, configuration, and
//	 * model files. Use this if you want to put the folder in a location 
//	 * different from the default. 
//	 * 
//	 * @param dataFolder	The data folder
//	 */
//	public static void setDataFolder(File dataFolder) {
//		FileUtils.setDataFolder(dataFolder);
//	}
}
