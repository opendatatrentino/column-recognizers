package eu.trentorise.opendata.columnrecognizers;
import java.util.Iterator;
import java.util.Set;

import eu.trentorise.opendata.nlprise.DataTypeGuess.Datatype;


/**
 * The ValueSetCR scores column-concept candidates by the extent to which the
 * content of the columns matches a model value set.
 * 
 * This is a column-based heuristic, i.e, it operates on one column at a time.
 * 
 * @author Simon
 *
 */
public class ValueSetCR extends ColumnContentBasedCR {
	/**
	 * The set of values indicating the concept we are testing for.
	 */
	private Set<String> valueSet = null;
	
	/**
	 * Creates the column recognizer.
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param valueSet		The set of values indicating the concept
	 * @param data			The table
	 */
	public ValueSetCR(String id, long conceptID, Set<String> valueSet, Table table) {
		super(id, conceptID, table);
		this.valueSet = valueSet;
	}
	
	@Override
	protected boolean isApplicableType(Datatype type) {
		return type == Datatype.STRING;
	}

	/**
	 * Compute the score for the column. The score is the fraction of cells 
	 * that match a member of the value set.
	 * 
	 * @param column	The column
	 * @return			The column score
	 */
	protected double computeColumnScore(Column column) {
		int matchCount = 0;
		int rowCount = column.size();
		Iterator<String> it = column.getFieldIterator();
		while (it.hasNext()) {
			if (valueSet.contains(CRStringUtils.normalize(it.next()))) {
				matchCount++;
			}
		}
		return ((double)matchCount) / rowCount;
	}

}
