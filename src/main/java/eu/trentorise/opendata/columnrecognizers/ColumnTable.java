package eu.trentorise.opendata.columnrecognizers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A ColumnTable stores its data as Columns. 
 * 
 * @author Simon
 *
 */
public class ColumnTable implements Table {
	/**
	 * The column headers
	 */
	private List<String> headers = null;
	
	/**
	 * The table columns
	 */
	private List<Column> columns = null;

	/**
	 * Constructs the ColumnTable
	 * 
	 * @param headers	The column headers
	 * @param columns	The columns
	 */
	public ColumnTable(List<String> headers, List<Column> columns) {
		super();
		this.headers = headers;
		this.columns = columns;
		assert(headers == null || headers.size() == columns.size());
	}
	
	/**
	 * Constructs ColumnTable from RowTable.
	 * 
	 * @param headers		The column headers
	 * @param rowTable		The RowTable
	 */
	public ColumnTable(List<String> headers, RowTable rowTable) {
		super();
		this.headers = headers;
		columns = rowTable.extractColumns();
	}

	/**
	 * Creates a new ColumnTable.
	 * 
	 * @param headers			The column headers
	 * @param columnStrings		The column data
	 * @return					The new table
	 */
	static ColumnTable makeColumnTableFromStringLists(List<String> headers, 
			List<List<String>> columnStrings) {
		List<Column> columnList = new ArrayList<Column>();
		Iterator<List<String>> it = columnStrings.iterator();
		while (it.hasNext()) {
			columnList.add(new Column(it.next()));
		}
		return new ColumnTable(headers, columnList);
	}
	
	/**
	 * Gets the column headers.
	 * 
	 * @return	The headers
	 */
	public List<String> getHeaders() {
		return headers;
	}
	
	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getColumnCount()
	 */
	public int getColumnCount() {
		return columns == null ? 0 : columns.size();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getRowCount()
	 */
	public int getRowCount() {
		return getColumnCount() == 0 ? 0 : extractColumn(1).size();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractColumn(int)
	 */
	public Column extractColumn(int columnNumber) {
		return columns.get(columnNumber - 1);
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractRowSample()
	 */
	public RowTable extractRowSample() {
		final char NULL_CHAR = '\0';
		final int INITIAL_ROW_SIZE = 100;
		final int SAMPLE_SIZE = 10;
		
		RowTable rowSample = new RowTable(NULL_CHAR);
		int rowCount = Math.min(SAMPLE_SIZE, getRowCount());
		int columnCount = getColumnCount();
		
		for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
			StringBuilder sb = new StringBuilder(INITIAL_ROW_SIZE);
			for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++) {
				sb.append(columns.get(columnNumber - 1).getFieldAt(rowIndex));
				if (columnNumber < columnCount) {
					sb.append(NULL_CHAR);
				}
			}
			rowSample.appendRow(sb.toString());
		}
		return rowSample;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getColumnFeatures()
	 */
	public List<List<Double>> getColumnFeatures() {
		List<List<Double>> columnFeatures = new ArrayList<List<Double>>(); 
		Iterator<Column> it = columns.iterator();
		while (it.hasNext()) {
			Column column = it.next();
			columnFeatures.add(column.getFeatures());
		}
		return columnFeatures;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractColumns()
	 */
	public List<Column> extractColumns() {
		return columns;
	}

}
