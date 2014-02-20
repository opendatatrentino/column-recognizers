package eu.trentorise.opendata.columnrecognizers;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	}
	
	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#extractRowSample()
	 */
	@Override
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
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return rows.size();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.Table#getColumnCount()
	 */
	@Override
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
	
	/**
	 * Extracts a single-column table corresponding to a column number.
	 * 
	 * @param columnNumber	The one-based index of the column to extract
	 * @return				The column
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
//		
//	/**
//	 * Extracts a list of the words occurring in the table, preserving their
//	 * order.
//	 * 
//	 * @return	The word list
//	 */
//	public List<String> extractWords() {
//		List<String> words = new ArrayList<String>();
//		Iterator<String> it = rows.iterator();
//		while (it.hasNext()) {
//			String row = it.next();
//			String[] rowWords = row.split("\\W+");
//			for (int i = 0; i < rowWords.length; i++) {
//				String word = CRStringUtils.normalize(rowWords[i]);
//				if (!word.isEmpty()) {
//					words.add(word);
//				}
//			}
//		}
//		return words;
//	}
//	
//	/**
//	 * Gets the set of terms in the table
//	 * 
//	 * @return	The set of terms
//	 */
//	public Set<String> extractWordSet() {
//		// Suppress the shortest words
//		final int MINIMAL_WORD_LENGTH = 3;
//
//		Set<String> words = new HashSet<String>();
//		Iterator<String> it = rows.iterator();
//		while (it.hasNext()) {
//			String row = it.next();
//			String[] rowWords = row.split("\\W+");
//			for (int i = 0; i < rowWords.length; i++) {
//				String word = CRStringUtils.normalize(rowWords[i]);
//				if (word.length() >= MINIMAL_WORD_LENGTH) {
//					words.add(word);
//				}
//			}
//		}
//		return words;
//	}
//	
//	/**
//	 * Computes the frequencies of words in the table.
//	 * 
//	 * @return	The word frequencies
//	 */
//	public Map<String, Integer> computeWordFrequencies () {
//		// Suppress the shortest words
//		final int MINIMAL_WORD_LENGTH = 3;
//		
//		Map<String, Integer> frequencies = new HashMap<String, Integer>();
//		List<String> words = extractWords();
//		Iterator<String> it = words.iterator();
//		while (it.hasNext()) {
//			String word = it.next();
//			if (word.length() >= MINIMAL_WORD_LENGTH) {
//				if (frequencies.containsKey(word)) {
//					frequencies.put(word, frequencies.get(word) + 1);
//				} else {
//					frequencies.put(word, 1);
//				}
//			}
//		}
//		return frequencies;
//	}
//
	
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
	 * Extracts all the columns from the table.
	 * This is currently very inefficient in that the column boundaries
	 * are calculated repeatedly.
	 * 
	 * @return	An array of the columns
	 */
	public Column[] extractColumns() {
		int columnCount = getColumnCount();
		Column[] columns = new Column[columnCount];
		for (int i = 0; i < columnCount; i++) {
			columns[i] = extractColumn(i + 1);
		}
		return columns;
	}
	
}
