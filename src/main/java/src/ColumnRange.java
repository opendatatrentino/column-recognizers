
/**
 * The class represents a range of table column using a one-based index.
 * 
 * Deprecated - use Set<Integer> to represent (also) non-contiguous sets of columns
 * 
 * @author Simon
 *
 */
public class ColumnRange {
	private static final int EMPTY_RANGE = -1;
	
	private int firstColumn = EMPTY_RANGE;
	private int lastColumn = EMPTY_RANGE;

	/**
	 * Creates specified range.
	 * 
	 * @param itsFirstColumn The start of the range (one-based index)
	 * @param itsLastColumn The end of the range
	 */
	public ColumnRange(int itsFirstColumn, int itsLastColumn) {
		firstColumn = itsFirstColumn;
		lastColumn = itsLastColumn;
	}
	
	/**
	 * Creates empty range.
	 */
	public ColumnRange() {
	}

	/**
	 * Returns the start of the range.
	 * 
	 * @return The start of the range
	 */
	public int getFirst() {
		return firstColumn;
	}
	
	/**
	 * Returns the end of the range.
	 * 
	 * @return The end of the range
	 */
	public int getLast() {
		return lastColumn;
	}
	
	/**
	 * Checks if the range is empty.
	 * 
	 * @return True if the range is empty
	 */
	public boolean empty() {
		return firstColumn == EMPTY_RANGE;
	}
}
