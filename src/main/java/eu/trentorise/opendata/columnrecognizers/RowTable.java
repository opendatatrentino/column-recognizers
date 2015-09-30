package eu.trentorise.opendata.columnrecognizers;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * The contents of a table represented as a list of rows.
 * The class is convenient for reading data from CSV files.
 * 
 * @author Simon
 *
 */

public class RowTable implements Table {
	/**
	 * Default column separator
	 */
	private static final char DEFAULT_COLUMN_SEPARATOR = ';';
	
	/**
	 * The table data, represented a list of rows
	 */
	private ArrayList<String> rows = new ArrayList<String>();
	
	/**
	 * The character used to separate columns
	 */
	private char columnSeparator = ';';
	
	/**
	 * Columns that have been extracted are cached for efficiency
	 */
	private List<Column> cachedColumns = null;
	
	/**
	 * Constructs the table. 
	 * 
	 * @param columnSeparator		The column separator character
	 */
	public RowTable(char columnSeparator) {
		super();
		this.columnSeparator = columnSeparator;
	}

	/**
	 * Sets the column separator.
	 * 
	 * @param columnSeparator The columnSeparator to set
	 */
	public void setColumnSeparator(char columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	/**
	 * Removes headers from the table.
	 * 
	 * @param numberOfRowsToRemove	The number of rows to remove
	 */
	public void removeHeaders(int numberOfRowsToRemove) {
		Iterator<String> it = rows.iterator();
		for (int i = 0; it.hasNext() && i < numberOfRowsToRemove; i++) {
			it.next();
			it.remove();
		}
		clearCaches();
	}
	
	/**
	 * Removes the first row containing the headers from the table.
	 * 
	 */
	public void removeHeaders() {
		assert(!rows.isEmpty());
		final int DEFAULT_NUMBER_OF_HEADER_ROWS = 1;
		removeHeaders(DEFAULT_NUMBER_OF_HEADER_ROWS);
	}

	
	/**
	 * Clear caches after modifying the table
	 */
	private void clearCaches() {
		cachedColumns = null;
	}

	/**
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractRowSample()
	 */
	public RowTable extractRowSample() {
		final int SAMPLE_SIZE = 10;

		RowTable sample = new RowTable(getColumnSeparator());
		Iterator<String> it = rows.iterator();		
		// Just take the first elements
		for (int i = 0; i < SAMPLE_SIZE && it.hasNext(); i++) {
			sample.appendRow(it.next());
		}
		
		return sample;
	}

	/**
	 * Append a row at the end of the table
	 * 
	 * @param row
	 */
	public void appendRow(String row) {
		rows.add(row);
		clearCaches();
	}

	/** 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return rows.toString();
	}

	/**
	 * Returns an iterator to the table rows
	 * 
	 * @return The row iterator
	 */
	public Iterator<String> getRowIterator() {
		return rows.iterator();
	}

	/**
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getRowCount()
	 */
	public int getRowCount() {
		return rows.size();
	}

	/**
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getColumnCount()
	 */
	public int getColumnCount() {
		String sampleRow = rows.get(0);
		return CSVProcessor.computeColumnCount(sampleRow, columnSeparator);
	}

	/**
	 * Returns the character that separates the cells in the string 
	 * representing a row.
	 * 
	 * @return	The separator character
	 */
	public char getColumnSeparator() {
		return columnSeparator;
	}

	/**
	 * Computes the positions of the column boundaries in a row.
	 * Note that column boundaries correspond to separator characters, so the
	 * number of boundaries will be one fewer than the number of columns.
	 * 
	 * @param row	The string representing the row
	 * @return		A list of column boundaries
	 */
	public List<Integer> findColumnBoundaries(String row) {
		return CSVProcessor.findColumnBoundaries(row, getColumnSeparator());
	}

	/**
	 * Returns a column number (1, ..) corresponding to a character position in
	 * the string representing a row.
	 * 
	 * @param columnBoundaries	The precomputed list of column boundaries
	 * @param charIndex			The character position
	 * @return					The column number
	 */
	public int getColumnFromCharIndex(List<Integer> columnBoundaries, int charIndex) {
		int columnNumber = 1;
		boolean done = false;
		while (!done && columnNumber <= columnBoundaries.size()) {
			if (charIndex > columnBoundaries.get(columnNumber - 1)) {
				columnNumber++;
			} else {
				done = true;
			}
		}
		return columnNumber;
	}
		
	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractColumn(int)
	 */
	public Column extractColumn(int columnNumber) {
		Column column = new Column();
		Iterator<String> it = rows.iterator();
		
		while (it.hasNext()) {
			String row = it.next();
			String[] fields = CSVProcessor.splitRecord(row, columnSeparator);
			String field = columnNumber <= fields.length ? fields[columnNumber - 1] : "";
			column.appendField(field);
		}
		
		return column;
	}

	/**
	 * Loads rows from a CSV file and appends them to the table.
	 * 
	 * @param csvFile	The input file
	 */
	public void loadRowsFromCSV(File csvFile) {
		CSVProcessor csv = new CSVProcessor(csvFile, this);
		csv.read();
	}

	/**
	 * Loads rows from a CSV input stream and appends them to the table.
	 * 
	 * @param csvFile	The input file
	 */
	public void loadRowsFromCSV(InputStream stream) {
		CSVProcessor csv = new CSVProcessor(stream, this);
		csv.read();
	}

	/**
	 * Loads a new row table from a CSV file.
	 * 
	 * @param csvFile				The input file
	 * @param columnSeparator		The column separator character
	 * @return						The new table
	 */
	public static RowTable loadFromCSV(File csvFile, char columnSeparator) {
		RowTable rowTable = new RowTable(columnSeparator);
		rowTable.loadRowsFromCSV(csvFile);
		return rowTable;
	}

	/**
	 * Loads a new row table from a CSV input stream.
	 * 
	 * @param stream				The input stream
	 * @param columnSeparator		The column separator character
	 * @return						The new table
	 */
	private static RowTable loadFromCSV(InputStream stream, char columnSeparator) {
		RowTable rowTable = new RowTable(columnSeparator);
		rowTable.loadRowsFromCSV(stream);
		return rowTable;
	}
	
	/**
	 * Prints the table to a file.
	 * 
	 * @param file	The file
	 */
	public void writeToFile(File file) {
		writeStringsToFile(rows.iterator(), file);
	}

	/**
	 * Writes strings, provided by an iterator, to a file.
	 * One string is output per line.
	 * 
	 * TODO create a StringIteratorLineWriter class for this
	 * 
	 * @param it		The iterator
	 * @param file		The output file
	 */
	static public void writeStringsToFile(Iterator<String> it,
			File file) {
	    Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(file), "utf-8"));
			while (it.hasNext()) {
				writer.write(String.format("%s%n", it.next()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	}

	/**
	 * Gets a value set from a file.
	 * 
	 * @param file	The input file
	 * @return		The set of values represented in the file
	 */
	public static Set<String> loadValueSet(File file) {
		RowTable table = loadFromCSV(file, DEFAULT_COLUMN_SEPARATOR);
		return table.extractColumn(1).getValueSet();
	}

	/**
	 * Gets a value set from an input stream.
	 * 
	 * @param file	The input file
	 * @return		The set of values represented in the file
	 */
	public static Set<String> loadValueSet(InputStream stream) {
		RowTable table = loadFromCSV(stream, DEFAULT_COLUMN_SEPARATOR);
		return table.extractColumn(1).getValueSet();
	}

	/**
	 * Extracts all the columns from the table.
	 * This is currently very inefficient in that the column boundaries
	 * are calculated repeatedly.
	 * 
	 * @return	An array of the columns
	 */
	public List<Column> extractColumns() {
		if (cachedColumns == null) {
			int columnCount = getColumnCount();
			cachedColumns = new ArrayList<Column>();
			for (int i = 0; i < columnCount; i++) {
				cachedColumns.add(extractColumn(i + 1));
			}
		}
		return cachedColumns;
	}

	public List<List<Double>> getColumnFeatures() {
		List<List<Double>> columnFeatures = new ArrayList<List<Double>>(); 
		List<Column> columns = extractColumns();
		Iterator<Column> it = columns.iterator();
		while (it.hasNext()) {
			Column column = it.next();
			columnFeatures.add(column.getFeatures());
		}
		return columnFeatures;
	}

	public List<String> getHeaders() {
		assert(!rows.isEmpty());
		String headerRow = rows.get(0);
		List<String> headers = new ArrayList<String>(Arrays.asList(
					CSVProcessor.splitRecord(headerRow, columnSeparator)));
		return headers;
	}

	/**
	 * Removes headers (first row) of table and returns the headers.
	 * 
	 * @return	The headers
	 */
	public List<String> popHeaders() {
		List<String> headers = getHeaders();
		removeHeaders();
		return headers;
	}
	
}
