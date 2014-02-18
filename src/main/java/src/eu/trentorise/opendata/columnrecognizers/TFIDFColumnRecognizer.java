package eu.trentorise.opendata.columnrecognizers;
/**
 * @author Simon
 *
 */
public class TFIDFColumnRecognizer extends ColumnContentBasedCR {
	/**
	 * The vector representing the prototype column
	 */
	TFIDFVector prototypeVector = null;
	
	/**
	 * The inverse column frequencies
	 */
	InverseColumnFrequency inverseFrequencies = null;
	
	/**
	 * Constructs the TFIDFColumnRecognizer.
	 * 
	 * @param id					A unique name for the recognizer instance
	 * @param conceptID				The knowledge base concept ID
	 * @param prototypeVector		The vector representing the prototype column
	 * @param inverseFrequencies	The inverse column frequencies
	 * @param table					The table (or a not-too-small sample of rows)
	 */
	public TFIDFColumnRecognizer(String id,
			long conceptID, 
			TFIDFVector prototypeVector, 
			InverseColumnFrequency inverseFrequencies,
			RowTable table) {
		super(id, conceptID, table);
		this.prototypeVector = prototypeVector;
		this.inverseFrequencies = inverseFrequencies;
	}

	/* (non-Javadoc)
	 * @see ColumnContentBasedCR#computeColumnScore(RowTable)
	 */
	@Override
	protected double computeColumnScore(RowTable column) {
		TFIDFVector observationVector = new TFIDFVector(column, inverseFrequencies);
		return observationVector.cosineSimilarity(prototypeVector);
	}

}
