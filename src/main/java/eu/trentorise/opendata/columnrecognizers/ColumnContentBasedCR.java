package eu.trentorise.opendata.columnrecognizers;
import it.unitn.disi.sweb.core.kb.model.types.attributes.DataType;

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
	 * True if the recognizer only works on columns of a specific type
	 */
	private boolean doRequireType = false;
	
	/**
	 * The data type that the recognizer works on
	 */
	private Datatype requiredType = null;
	
//	/**
//	 * Creates the column recognizer. 
//	 * Deprecated - use the constructor that takes a Table instead.
//	 * 
//	 * @param id			A unique name for the recognizer instance
//	 * @param conceptID		The knowledge base concept ID
//	 * @param table			The table (or a not-too-small sample of rows)
//	 */
//	public ColumnContentBasedCR(String id, long conceptID, RowTable table) {
//		super(id, conceptID, table);
//	}

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
	
	/**
	 * Constructs the recognizer with a required type.
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param requiredType	The type that the recognizer operates on
	 * @param table			The table 
	 */
	public ColumnContentBasedCR(
			String id, 
			long conceptID, 
			Datatype requiredType,
			Table table) {
		super(id, conceptID, table);
		doRequireType = true;
		this.requiredType = requiredType;
	}

	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		List<Column> columns = getTable().extractColumns();
		int columnNumber = 1;
		for (Column column : columns) {
			if (doesRequireType() && getRequiredType() == column.getType()) {
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
	 * Returns the required type.
	 * 
	 * @return		The required type
	 */
	public Object getRequiredType() {
		return requiredType;
	}

	/**
	 * Returns true if the recognizer only operates on a given type.
	 * 
	 * @return 		True if it has a required type
	 */
	public boolean doesRequireType() {
		return doRequireType;
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
