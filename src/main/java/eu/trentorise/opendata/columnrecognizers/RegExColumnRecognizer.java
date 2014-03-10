package eu.trentorise.opendata.columnrecognizers;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The RegExColumnRecognizer provides basic functionality for guessing the
 * concept represented by a column by applying regular expressions to rows.
 * 
 * In its present form, the recognizer is associated with a single concept ID
 * and a single regular expression.
 * 
 * The recognizer takes as input a sample of the rows in the table and applies
 * the regular expression to each row. For each row where the reg ex matches,
 * it determines which columns were covered by the match. The score for each
 * column is defined as the fraction of the total number of rows where the 
 * column was covered.
 * 
 * Example: We try to detect street addresses. In 40% of the rows, the reg ex
 * matches and the match includes the text of column 3, which represents the
 * house number ('civico'). Column 3 gets a score of 0.4.
 * 
 * @author Simon
 *
 */

public class RegExColumnRecognizer extends RowBasedCR {
	private Pattern pattern = null; 
	
	/**
	 * Constructs the reg ex recognizer. 
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param itsConceptID	The semantic concept to test for
	 * @param itsRegEx		The pattern used to detect the concept
	 * @param itsData		The sample of rows
	 */
	public RegExColumnRecognizer(String id,
			long conceptID, 
			String regEx,
			RowTable table) {
		super(id, conceptID, table);
		pattern = Pattern.compile(regEx);
	}
	
	/**
	 * Applies the reg ex to a row and returns the range of covered columns.
	 * 
	 * @param row	The row to test
	 * @return		Covered columns (empty if no match)
	 */
	public Set<Integer> applyRegEx(String row) {
		Set<Integer> columnSet = new HashSet<Integer>();
		Matcher matcher = pattern.matcher(row);
		List<Integer> columnBoundaries = getRowTable().findColumnBoundaries(row);
		
		while (matcher.find()) {
			int firstColumn = getRowTable().getColumnFromCharIndex(columnBoundaries, matcher.start());
			int lastColumn = getRowTable().getColumnFromCharIndex(columnBoundaries, matcher.end());
			for (int columnNumber = firstColumn; columnNumber <= lastColumn; columnNumber++) {
				columnSet.add(columnNumber);
			}
		}
		
		return columnSet;
	}

	@Override
	protected Set<Integer> computeColumnMatches(String row) {
		return applyRegEx(row);
	}	
	
	
}
