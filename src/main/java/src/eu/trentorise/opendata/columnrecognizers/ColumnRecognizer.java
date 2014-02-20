package eu.trentorise.opendata.columnrecognizers;
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
	
}
