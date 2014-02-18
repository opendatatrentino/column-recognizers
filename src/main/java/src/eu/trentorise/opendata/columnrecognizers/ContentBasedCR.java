package eu.trentorise.opendata.columnrecognizers;
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
	 * @param id			A unique name for the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param table			The data
	 */
	public ContentBasedCR(String id, long conceptID, RowTable table) {
		super(id);
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
