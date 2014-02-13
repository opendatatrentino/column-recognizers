import java.util.ArrayList;
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
	 * Creates the column recognizer.
	 * 
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The data
	 */
	public RowBasedCR(long conceptID, RowTable table) {
		super(conceptID, table);
	}

	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public List<ColumnConceptCandidate> computeScoredCandidates() {
		int[] columnMatches = new int[getTable().getColumnCount()];
		countColumnMatches(columnMatches);
		return computeCandidates(columnMatches);
	}
	/**
	 * For each column, counts the number of rows in which the regular 
	 * expression matches and the match covers the column.
	 * 
	 * @param columnMatches
	 */
	private void countColumnMatches(int[] columnMatches) {
		Iterator<String> it = getTable().getRowIterator();
		while (it.hasNext()) {
			String row = it.next();
			row = normalize(row);
			Set<Integer> columnSet = computeColumnMatches(row);
			
			Iterator<Integer> itColumnNumber = columnSet.iterator();
			while (itColumnNumber.hasNext()) {
				columnMatches[itColumnNumber.next() - 1]++;				
			}
		}
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
	 * @return				The scored column-concept candidates
	 */
	private List<ColumnConceptCandidate> computeCandidates(int[] columnMatches) {
		int rowCount = getTable().getRowCount();
		ArrayList<ColumnConceptCandidate> candidates 
			= new ArrayList<ColumnConceptCandidate>();
		int columnCount = columnMatches.length;
		
		for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
			if (columnMatches[columnNumber - 1] > 0) {
				ColumnConceptCandidate candidate 
					= new ColumnConceptCandidate(columnNumber, getConceptID());
				candidate.setScore(((double)columnMatches[columnNumber - 1]) / rowCount);
				candidates.add(candidate);
			}
		}
		return candidates;
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
