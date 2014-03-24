package eu.trentorise.opendata.columnrecognizers;
import java.util.List;

import eu.trentorise.opendata.nlprise.DataTypeGuess.Datatype;


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
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The table 
	 */
	public ColumnContentBasedCR(
			String id, 
			long conceptID, 
			Table table) {
		super(id, conceptID, table);
	}
	
	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		List<Column> columns = getTable().extractColumns();
		int columnNumber = 1;
		for (Column column : columns) {
			if (isApplicableType(column.getType())) {
			double score = computeColumnScore(column);
				if (score > 0) {
					ColumnConceptCandidate newCandidate 
						= new ColumnConceptCandidate(columnNumber, getConceptID(), score, getId());
					candidates.add(newCandidate);
				}
			}
			columnNumber++;
		}
	}
	
	/**
	 * Returns true if the recognizer can operate on this column data type. 
	 * True by default. Override if your recognizer doesn't apply to all types.
	 * 
	 * @param type		The column data type
	 * @return			True if the type applies
	 */
	protected boolean isApplicableType(Datatype type) {
		return true;
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
