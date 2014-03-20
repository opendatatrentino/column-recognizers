package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * The TFIDF vector represents a column as term frequency - inverse document 
 * frequency vector.
 * 
 * Consult the following Wikipedia articles for some theoretical background:
 * 
 * http://en.wikipedia.org/wiki/Vector_space_model
 * http://en.wikipedia.org/wiki/Tf-idf
 * 
 * @author Simon
 *
 */
public class TFIDFVector {
	/**
	 * The terms and their associated component values (TF * IDF)
	 */
	Map<String, Double> vector = null;
	
	/**
	 * Constructs the TFIDF vector.
	 * 
	 * @param column				The column
	 * @param inverseFrequencies	The inverse column frequencies
	 */
	public TFIDFVector(Column column, InverseColumnFrequency inverseFrequencies) {
		super();
		Map<String, Integer> termFrequencies = column.computeWordFrequencies();
		vector = new HashMap<String, Double>();
		for (Entry<String, Integer> termFrequency : termFrequencies.entrySet()) {
			String term = termFrequency.getKey();
			Integer frequency = termFrequency.getValue();
			double weight = inverseFrequencies.getInverseFrequency(term);
			double component = ((double)frequency) * weight;
			vector.put(term, component); 
		}
	}
	
	/**
	 * Constructs the TFIDF vector.
	 * 
	 * @param vector	The term component values
	 */
	private TFIDFVector(Map<String, Double> vector) {
		super();
		this.vector = vector;
	}
	
	/**
	 * Builds a prototype model file. 
	 * <p>
	 * The arguments:
	 * 	<csv_file> <column_separator> <column_number> <inverse_freq_file> <output_file>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final int MIN_ARG_COUNT = 5;
		final int NUMBER_OF_HEADER_ROWS = 1;
		
		if (args.length < MIN_ARG_COUNT) {
			System.out.println("Usage: TFIDFVector <csv_file_path> <column_separator> " +
					"<column_number> <inverse_freq_file> <output_file>");
			System.exit(1);
		}
		
		int argIndex = 0;
		
		String csvPath = args[argIndex++];
		char columnSeparator = args[argIndex++].charAt(0);
		int columnNumber = Integer.parseInt(args[argIndex++]);
		String inverseFreqPath = args[argIndex++];
		String outputPath = args[argIndex++];
		
		RowTable table = RowTable.loadFromCSV(new File(csvPath), columnSeparator);
		table.removeHeaders(NUMBER_OF_HEADER_ROWS);
		Column column = table.extractColumn(columnNumber);
		File inverseFrequencyFile = new File(inverseFreqPath);
		InverseColumnFrequency inverseFrequencies 
			= InverseColumnFrequency.readFromFile(inverseFrequencyFile);
		TFIDFVector prototype = new TFIDFVector(column, inverseFrequencies);
		File outputFile = new File(outputPath);
		prototype.writeToFile(outputFile);
	}
	
	/**
	 * Saves the vector to a file.
	 * 
	 * @param file	The output file
	 */
	public void writeToFile(File file) {
		WordScoreWriter writer = new WordScoreWriter(file, vector);
		writer.write();
	}
	
	/**
	 * Loads a vector from a file.
	 * 
	 * @param file	The input file
	 * @return		The new vector
	 */
	public static TFIDFVector readFromFile(File file) {
		WordScoreReader reader = new WordScoreReader(file);
		reader.read();
		Map<String, Double> vector = reader.getWordScores();
		return new TFIDFVector(vector);
	}
	
	/**
	 * Calculates the cosine between two vectors.
	 * 
	 * @param other		The other vector
	 * @return			The cosine
	 */
	public double cosineSimilarity(TFIDFVector other) {
		double denominator = norm() * other.norm();
		return denominator == 0 ? 0 : innerProduct(other) / denominator;
	}

	/**
	 * Calculates the inner product of two vectors
	 * 
	 * @param other		The other vector
	 * @return			The inner product
	 */
	public double innerProduct(TFIDFVector other) {
		double innerProduct = 0;
		for (Entry<String, Double> termComponent : vector.entrySet()) {
			String term = termComponent.getKey();
			double component = termComponent.getValue();
			if (other.contains(term)) {
				innerProduct += component * other.getComponent(term);
			}
		}
		return innerProduct;
	}

	/**
	 * Calculates the length of the vector.
	 * 
	 * @return	The vector norm
	 */
	public double norm() {
		double sumOfSquares = 0;
		for (double component : vector.values()) {
			sumOfSquares += Math.pow(component, 2);
		}
		return Math.sqrt(sumOfSquares);
	}

	/**
	 * Returns true if the vector has a component corresponding to the term.
	 * 
	 * @param term		The term
	 * @return			True if component exists
	 */
	public boolean contains(String term) {
		return vector.containsKey(term);
	}

	/**
	 * Returns the vector component value associated with the term.
	 * 
	 * @param term		The term
	 * @return			The vector component value
	 */
	public double getComponent(String term) {
		return vector.get(term);
	}
}
