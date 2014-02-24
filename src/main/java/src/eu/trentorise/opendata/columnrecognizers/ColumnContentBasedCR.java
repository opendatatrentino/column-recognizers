package eu.trentorise.opendata.columnrecognizers;
import java.util.List;


/**
 * Abstract base class for column-content-based column recognizers.
 * Subclasses need to implement the computeColumnScore method, assigning a
 * score to a column.
 * 
 * @author Simon
 *
 */
public abstract class ColumnContentBasedCR extends ContentBasedCR {
	/**
	 * Creates the column recognizer. 
	 * Deprecated - use the constructor that takes a Table instead.
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The table (or a not-too-small sample of rows)
	 */
	public ColumnContentBasedCR(String id, long conceptID, RowTable table) {
		super(id, conceptID, table);
	}

	/**
	 * Creates the column recognizer. 
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The table 
	 */
	public ColumnContentBasedCR(String id, long conceptID, Table table) {
		super(id, conceptID, table);
	}

	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		int columnCount = getTable().getColumnCount();
		for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
			Column column = getTable().extractColumn(columnNumber);
			double score = computeColumnScore(column);
			if (score > 0) {
				ColumnConceptCandidate newCandidate 
					= new ColumnConceptCandidate(columnNumber, getConceptID());
				newCandidate.setScore(score);
				candidates.add(newCandidate);
			}
		}
	}
	
	/**
	 * Implementations of this abstract method computes a score given the data
	 * for a column.
	 * 
	 * @param column	The column data
	 * @return			The score
	 */
	protected abstract double computeColumnScore(Column column);

}
