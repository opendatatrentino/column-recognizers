package eu.trentorise.opendata.columnrecognizers;

import java.util.List;

/**
 * @author Simon
 *
 */
public abstract class HeaderBasedCR extends ColumnRecognizer {

    /**
	 * The column headers
	 */
	private List<String> headers = null;

	/**
	 * Constructs the HeaderBasedCR.
	 * 
	 * @param id		The identifier for the recognizer
	 * @param table		The data table
	 */
	public HeaderBasedCR(String id, Table table) {
		super(id);
		headers = table.getHeaders();
	}

	/**
	 * Gets the column headers of the data table.
	 * 
	 * @return The headers
	 */
	public List<String> getHeaders() {
		return headers;
	}

	
}
