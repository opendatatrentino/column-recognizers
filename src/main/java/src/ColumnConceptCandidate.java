import java.util.Locale;


/**
 * The ColumnConceptCandidate class represents a scored column-concept 
 * candidate.
 * 
 * @author Simon
 *
 */
public class ColumnConceptCandidate {
	/**
	 * The one-based column index
	 */
	private int columnNumber = 0;
	
	/**
	 * The knowledge base concept ID
	 */
	private long conceptID = -1;
	
	/**
	 * The confidence score -- can be any real number but is often confined to
	 * 	the interval [0, 1]
	 */
	private double score = 0;
	
	/**
	 * Creates the column-concept candidate.
	 * 
	 * @param itsColumnNumber	The index (1, ..) of the column
	 * @param itsConceptID		The suggested UK concept ID
	 */
	ColumnConceptCandidate(int itsColumnNumber, long itsConceptID) {
		columnNumber = itsColumnNumber;
		conceptID = itsConceptID;
	}

	/**
	 * Gets the score assigned to this candidate.
	 * 
	 * @return	The score
	 */
	public double getScore() {
		return score;
	}
	
	/**
	 * Assigns a score to this candidate.
	 * 
	 * @param itsScore	The score
	 */
	void setScore(double itsScore) {
		score = itsScore;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(Locale.US, 
				"Column = %d; Concept = %d;  Score = %f", 
				columnNumber, 
				conceptID, 
				score);
	}

	/**
	 * Get the (column number, concept ID) pair
	 * 
	 * @return	The column-concept pair
	 */
	public ColumnConcept getColumnConcept() {
		return new ColumnConcept(columnNumber, conceptID);
	}
	
	
}
