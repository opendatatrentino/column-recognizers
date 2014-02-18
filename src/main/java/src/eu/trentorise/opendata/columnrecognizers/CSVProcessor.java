package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * The class reads CSV files and processes CSV data.
 * It handles quote-delimited fields that can contain quotes (represented by
 * two quotes together) and line breaks.
 * 
 * Multi-line records are identified using the fact that a record is complete
 * iff the number of quote characters in the record is even.
 * 
 * @author Simon
 *
 */
public class CSVProcessor extends LineReader {
	private RowTable table = null;
	private String csvRecord = null;
	private int csvRecordQuoteParity = 0;
	private static final char QUOTE_CHAR = '"';
	
	/**
	 * Constructs the CSV processor.
	 * 
	 * @param file		The CSV-formatted input file
	 * @param table		The table to which the records will be appended
	 */
	public CSVProcessor(File file, RowTable table) {
		super(file);
		this.table = table;
	}

	/* (non-Javadoc)
	 * @see LineReader#processLine(java.lang.String)
	 */
	@Override
	protected void processLine(String line) {
		assert(csvRecordQuoteParity == 1 && csvRecord != null 
				|| csvRecordQuoteParity == 0 && csvRecord == null);
		if (hasIncompleteRecord()) {
			appendToRecord(line);
		} else {
			csvRecord = line;
			csvRecordQuoteParity = quoteParity(line);
		}
		
		if (recordIsComplete()) {
			table.appendRow(csvRecord);
			resetCSVRecord();
		}
	}
		
	/**
	 * Removes the CSV record after completion.
	 */
	private void resetCSVRecord() {
		csvRecord = null;
		csvRecordQuoteParity = 0;
	}

	/**
	 * Returns true if the record has been completed.
	 * 
	 * @return	True if the record has been completed
	 */
	private boolean recordIsComplete() {
		assert(csvRecord != null);
		return csvRecordQuoteParity == 0;
	}

	/**
	 * Returns true if there is an incomplete CSV record in progress.
	 * 
	 * @return	True if there is an incomplete record
	 */
	private boolean hasIncompleteRecord() {
		return csvRecordQuoteParity == 1;
	}

	/**
	 * Returns the parity (even or odd) of the number of quote characters in 
	 * the string.
	 * 
	 * @param string	The string where we count the quotes
	 * @return			The parity (0 or 1)
	 */
	private int quoteParity(String string) {
		return CRStringUtils.countOccurrences(string, '"') % 2;
	}
	
	/**
	 * Appends the line to the CSV record being built.
	 * 
	 * @param line	The line read from the CSV file
	 */
	private void appendToRecord(String line) {
		csvRecord = String.format("%s%n%s", csvRecord, line);
		csvRecordQuoteParity = (csvRecordQuoteParity + quoteParity(line)) % 2;
	}
	
	/**
	 * Computes the positions of the column boundaries in a csv record.
	 * Note that column boundaries correspond to separator characters, so the
	 * number of boundaries will be one fewer than the number of columns.
	 * 
	 * @param record	The string representing the record
	 * @return			A list of column boundaries
	 */
	public static List<Integer> findColumnBoundaries(String record, char columnSeparator) {
		ArrayList<Integer> columnBoundaries = new ArrayList<Integer>();
		int separatorPosition = 0;
		int fromIndex = 0;
		boolean foundBoundary = false;
		
		do {
			separatorPosition = scanToBoundary(record, columnSeparator, fromIndex);
			foundBoundary = separatorPosition != -1;
			if (foundBoundary) {
				columnBoundaries.add(separatorPosition);
				fromIndex = separatorPosition + 1;
			}			
		} while (foundBoundary);
		
		return columnBoundaries;
	}
	
	/**
	 * Finds the next column boundary in a record.
	 * 
	 * @param record				The csv record
	 * @param columnSeparator		The column separator character
	 * @param fromIndex				The place to start
	 * @return						The position of the next column separator or -1 if none
	 */
	private static int scanToBoundary(String record, char columnSeparator, int fromIndex) {
		int position = scanPastWhiteSpace(record, fromIndex);
		boolean isQuotedField = (position < record.length() && record.charAt(position) == QUOTE_CHAR);
		if (isQuotedField) {
			position++;
			position = scanPastClosingQuote(record, position);
		}
		return record.indexOf(columnSeparator, position);
	}

	/**
	 * Skips white space.
	 * 
	 * @param record		The text being scanned
	 * @param position		The starting position
	 * @return				The new position
	 */
	private static int scanPastWhiteSpace(String record, int position) {
		while (position < record.length() && Character.isWhitespace(record.charAt(position))) {
			position++;
		}
		return position;
	}
	
	/**
	 * In a quoted field, finds the position after the closing quote.
	 * 
	 * @param record		The text being scanned
	 * @param position		The starting position
	 * @return				The new position
	 */
	private static int scanPastClosingQuote(String record, int position) {
		boolean foundClosingQuote = false;
		do {
			position = record.indexOf(QUOTE_CHAR, position);
			int nextPosition = position + 1;
			boolean isEscapedQuote 
				= (nextPosition < record.length() && record.charAt(nextPosition) == QUOTE_CHAR);
			if (isEscapedQuote) {
				position += 2;
			} else {
				foundClosingQuote = true;
			}
		} while (!foundClosingQuote);
		return position;
	}

	/**
	 * Splits the CSV record into its fields.
	 * 
	 * @param record			The CSV record
	 * @param columnSeparator	The column separator character
	 * @return					The fields
	 */
	public static String[] splitRecord(String record, char columnSeparator) {
		List<Integer> columnBoundaries = findColumnBoundaries(record, columnSeparator);
		String[] fields = new String[columnBoundaries.size() + 1];
		int lastSeparatorPosition = -1;
		int fieldIndex = 0;
		Iterator<Integer> it = columnBoundaries.iterator();
		while (it.hasNext()) {
			int newSeparatorPosition = it.next();
			fields[fieldIndex] = record.substring(lastSeparatorPosition + 1, newSeparatorPosition);
			lastSeparatorPosition = newSeparatorPosition;
			fieldIndex++;
		}
		fields[fieldIndex] = record.substring(lastSeparatorPosition + 1);
		
		return fields;
	}

	/**
	 * Calculates the number of columns in a record.
	 * 
	 * @param record			The CSV record
	 * @param columnSeparator	The column separator character
	 * @return					The number of columns
	 */
	public static int computeColumnCount(String record, char columnSeparator) {
		return findColumnBoundaries(record, columnSeparator).size() + 1;
	}
	
}
