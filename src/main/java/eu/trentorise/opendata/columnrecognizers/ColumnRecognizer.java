package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


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
	 * Static API method for computing column-concept candidates for a table.
	 * 
	 * @param columnHeaders	The column headers
	 * @param columnData	The column contents
	 * @return				The column-concept candidates
	 */
	public static List<ColumnConceptCandidate> computeScoredCandidates(
		    List<String> columnHeaders,
		    List<List<String>> columnData) {
		final String SPECIFICATION_PATH = "column-recognizers.txt";
		ColumnTable columnTable = ColumnTable.makeColumnTableFromStringLists(columnHeaders, columnData);
		RowTable rowSample = columnTable.extractRowSample();
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		CompositeColumnRecognizer compositeCR = new CompositeColumnRecognizer("composite");
		File specificationFile = new File(SPECIFICATION_PATH);
		ColumnRecognizerFactory.attachRecognizers(compositeCR, specificationFile, columnTable, rowSample);
		compositeCR.computeScoredCandidates(candidates);
		
		return candidates;
	}

	/**
	 * Sets the folder with the SVM-Light executables. Use this if you want
	 * to put the folder in a location different from the default.
	 * 
	 * @param svmExecutablesFolder The folder with the SVM-Light executables
	 */
	public static void setSVMExecutablesFolder(File svmExecutablesFolder) {
		FileUtils.setSVMExecutablesFolder(svmExecutablesFolder);
	}

	/**
	 * Sets the folder containing column recognizer data, configuration, and
	 * model files. Use this if you want to put the folder in a location 
	 * different from the default. 
	 * 
	 * @param dataFolder	The data folder
	 */
	public static void setDataFolder(File dataFolder) {
		FileUtils.setDataFolder(dataFolder);
	}
}
