/**
 * 
 */
package eu.trentorise.opendata.columnrecognizers;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Simon
 *
 */
public class HeaderRegExCR extends HeaderBasedCR {
	/**
	 * The concept being recognized
	 */
	private long conceptID = 0;

	/**
	 * The regular expression pattern
	 */
	private Pattern pattern = null; 
	
	/**
	 * The score to assign to column-concept candidates
	 */
	private double score = 0;

	/**
	 * Constructs the HeaderRegExCR.
	 * 
	 * @param id	The recognizer identifier
	 * @param table	The data table
	 */
	public HeaderRegExCR(
			String id, 
			long conceptID, 
			String regEx,
			double score,
			Table table) {
		super(id, table);
		this.conceptID = conceptID;
		pattern = Pattern.compile(regEx);
		this.score = score;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.ColumnRecognizer#computeScoredCandidates(java.util.List)
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		int columnNumber = 1;
		Iterator<String> itHeader = getHeaders().iterator();
		while (itHeader.hasNext()) {
			String header = itHeader.next();
			Matcher matcher = pattern.matcher(header);
			if (matcher.matches()) {
				ColumnConceptCandidate candidate 
					= new ColumnConceptCandidate(columnNumber, conceptID, score, getId());
				candidates.add(candidate);
			}
			columnNumber++;
		}

	}

}
