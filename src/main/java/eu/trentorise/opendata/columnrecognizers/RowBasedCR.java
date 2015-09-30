package eu.trentorise.opendata.columnrecognizers;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Abstract superclass for row-based column recognizers. 
 * 
 * @author Simon
 *
 */
public abstract class RowBasedCR extends ContentBasedCR {
	/**
	 * Reference to the table as RowTable
	 */
	private RowTable rowTable = null;
	
	/*
	 * True if comparisons are case sensitive
	 */
	private boolean isCaseSensitive = false;
	
	/**
	 * Creates the column recognizer.
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The data
	 */
	public RowBasedCR(String id, long conceptID, RowTable table) {
		super(id, conceptID, table);
		rowTable = table;
	}
	
	/**
	 * Returns a reference to the table as a RowTable
	 * 
	 * @return	The RowTable
	 */
	RowTable getRowTable() {
		return rowTable;
	}

	/**
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		int[] columnMatches = new int[getTable().getColumnCount()];
		countColumnMatches(columnMatches);
		computeCandidates(columnMatches, candidates);
	}
	
	/**
	 * For each column, counts the number of rows in which the regular 
	 * expression matches and the match covers the column.
	 * 
	 * @param columnMatches
	 */
	private void countColumnMatches(int[] columnMatches) {
		Iterator<String> it = getRowTable().getRowIterator();
		while (it.hasNext()) {
			String row = it.next();
			if (!caseSensitive()) {
				row = normalize(row);			
			}
			Set<Integer> columnSet = computeColumnMatches(row);
			
			Iterator<Integer> itColumnNumber = columnSet.iterator();
			while (itColumnNumber.hasNext()) {
				columnMatches[itColumnNumber.next() - 1]++;				
			}
		}
	}

	/**
	 * Returns true if comparison / matching is done case sensitively
	 * 
	 * @return		True if case sensitive
	 */
	public boolean caseSensitive() {
		return isCaseSensitive;
	}
	
	/**
	 * Sets the recognizer to be case sensitive.
	 */
	public void beCaseSensitive() {
		isCaseSensitive = true;
	}

	/**
	 * Returns the set of matching columns given a row. Subclasses must 
	 * override to provide the row-based heuristic.
	 * 
	 * @param row	The row
	 * @return		The column numbers of any matching columns
	 */
	protected abstract Set<Integer> computeColumnMatches(String row);

	/**
	 * Computes the column-concept candidates from the column match counts.
	 * 
	 * @param columnMatches	The column match counts
	 * @param candidates	The scored column-concept candidates
	 */
	private void computeCandidates(int[] columnMatches,
			List<ColumnConceptCandidate> candidates) {
		int rowCount = getTable().getRowCount();
		int columnCount = columnMatches.length;
		
		for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
			if (columnMatches[columnNumber - 1] > 0) {
				double score = ((double)columnMatches[columnNumber - 1]) / rowCount;
				ColumnConceptCandidate candidate 
					= new ColumnConceptCandidate(columnNumber, getConceptID(), score, getId());
				candidates.add(candidate);
			}
		}
	}

	/**
	 * Does preprocessing on the row. 
	 * 
	 * @param row	The string representing the row
	 * @return 		The normalized version of the string
	 */
	private String normalize(String row) {
		return CRStringUtils.normalize(row);
	}

}
