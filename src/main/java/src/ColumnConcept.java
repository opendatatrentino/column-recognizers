

/**
 * The ColumnConcept class represents a (column number, concept ID) pair.
 * It can be used as a HashMap key.
 * 
 * @author Simon
 *
 */
public class ColumnConcept {
	/**
	 * The one-based column index
	 */
	public int columnNumber = 0;
	
	/**
	 * The knowledge base concept ID
	 */
	public long conceptID = -1;

	/**
	 * Creates the column-concept pair.
	 * 
	 * @param itsColumnNumber	The index (1, ..) of the column
	 * @param itsConceptID		The suggested UK concept ID
	 */
	ColumnConcept(int itsColumnNumber, long itsConceptID) {
		columnNumber = itsColumnNumber;
		conceptID = itsConceptID;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columnNumber;
		result = prime * result + (int) (conceptID ^ (conceptID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ColumnConcept other = (ColumnConcept) obj;
		if (columnNumber != other.columnNumber) {
			return false;
		}
		if (conceptID != other.conceptID) {
			return false;
		}
		return true;
	}
	
	
	
}
