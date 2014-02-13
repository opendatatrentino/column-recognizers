/**
 * The abstract superclass for all content-based column recognizers.
 * 
 * @author Simon
 *
 */
public abstract class ContentBasedCR extends ColumnRecognizer {
	/**
	 * The knowledge base concept ID
	 */
	private long conceptID = -1;
	
	/**
	 * The data table
	 */
	private RowTable table = null;

	/**
	 * Creates the column recognizer.
	 * 
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The data
	 */
	public ContentBasedCR(long conceptID, RowTable table) {
		super();
		this.conceptID = conceptID;
		this.table = table;
	}
	
	/**
	 * Gets the knowledge base concept ID.
	 * 
	 * @return	The concept ID
	 */
	public long getConceptID () {
		return conceptID;
	}
	
	/**
	 * Gets the table data.
	 * 
	 * @return	The data
	 */
	protected RowTable getTable() {
		return table;
	}

}
