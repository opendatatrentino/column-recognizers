package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * The InverseColumnFrequency class calculates the inverse document 
 * frequency (IDF), a measure of how rare a term is across documents, for terms
 * in database tables. In this context, the notional 'documents' are table 
 * columns, not files.
 * 
 * When we use similarity of term frequency as a measure of the similarity
 * between database columns, terms can be weighted by their IDF. 
 * 
 * Consult the following Wikipedia articles for some theoretical background:
 * 
 * http://en.wikipedia.org/wiki/Vector_space_model
 * http://en.wikipedia.org/wiki/Tf-idf
 * 
 * @author Simon
 *
 */
public class InverseColumnFrequency {
	/**
	 * The key to the extra entry in the map of inverse frequencies 
	 * representing the words not found in the corpus. 
	 */
	private final static String UNSEEN_WORD = "___UNSEEN___";
	
	/**
	 * The inverse frequency for each word
	 */
	Map<String, Double> inverseFrequencies = null;
	
	/**
	 * Constructs the InverseColumnFrequency table from a corpus.
	 * 
	 * @param frequencies	The number of columns each word occurs in
	 * @param columnCount	The total number of columns in the corpus
	 */
	public InverseColumnFrequency(Map<String, Integer> frequencies, int columnCount) {
		super();
		calculate(frequencies, columnCount);
	}
	
	/**
	 * Constructs the InverseColumnFrequency table from the inverse 
	 * frequency map.
	 * 
	 * @param inverseFrequencies	The words and their inverse frequencies
	 */
	private InverseColumnFrequency(Map<String, Double> inverseFrequencies) {
		super();
		this.inverseFrequencies = inverseFrequencies;
	}

	/**
	 * Compute the inverse frequencies from a corpus consisting of the CSV 
	 * tables given as arguments. The result is output to a file.
	 * 
	 * @param args	The arguments: file_path, column_separator, ...
	 */
	public static void main(String[] args) {
		final String OUTPUT_FILE_NAME = "inverse-frequencies.txt";
		
		// TODO some argument checking
		
		// The number of columns each word occurs in
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		// The number of columns
		int columnCount = 0;
		
		// We assume that arguments are path, column_separator, ...
		for (int i = 0; i < args.length; i += 2) {
			String pathName = args[i];
			char columnSeparator = args[i + 1].charAt(0);
			File csvFile = new File(pathName);
			RowTable table = RowTable.loadFromCSV(csvFile, columnSeparator);
			RowTable[] columns = table.extractColumns();
			for (RowTable column: columns) {
				InverseColumnFrequency.addColumn(column, frequencies);
				columnCount++;
			}
		}
		
		InverseColumnFrequency inverseFrequencies 
			= new InverseColumnFrequency(frequencies, columnCount);
		inverseFrequencies.writeToFile(new File(OUTPUT_FILE_NAME));
	}

	/**
	 * Adds the words of a column to the column frequencies.
	 * 
	 * @param column		The column
	 * @param frequencies	The number of columns each word occurs in
	 */
	private static void addColumn(RowTable column, Map<String, Integer> frequencies) {
		Set<String> words = column.extractWordSet();
		for (String word: words) {
			incrementCount(word, frequencies);
		}
	}

	/**
	 * Adds a word occurrence.
	 * 
	 * @param word			The word
	 * @param frequencies	The number of columns each word occurs in
	 */
	private static void incrementCount(String word, Map<String, Integer> frequencies) {
		if (frequencies.containsKey(word)) {
			frequencies.put(word, frequencies.get(word) + 1);
		} else {
			frequencies.put(word, 1);
		}
	}

	/**
	 * Calculates the inverse frequencies from the column frequencies and 
	 * the total number of columns.
	 * 
	 * @param frequencies	The number of columns each word occurs in
	 * @param columnCount	The total number of columns in the corpus
	 */
	private void calculate(Map<String, Integer> frequencies, int columnCount) {
		inverseFrequencies = new HashMap<String, Double>();
		for (Entry<String, Integer> wordFrequency: frequencies.entrySet()) {
			String word = wordFrequency.getKey();
			int frequency = wordFrequency.getValue();
			double inverseCount = ((double)columnCount) / (frequency + 1);
			inverseFrequencies.put(word, Math.log(inverseCount));
		}
		inverseFrequencies.put(UNSEEN_WORD, getZeroFrequencyIDF(columnCount));
	}

	/**
	 * Writes out the inverse frequencies to a text file.
	 * 
	 * @param file	The output text file
	 */
	private void writeToFile(File file) {
		WordScoreWriter writer = new WordScoreWriter(file, inverseFrequencies);
		writer.write();
	}
	
	/**
	 * Return the inverse frequency assigned to words that do not occur in the
	 * corpus.
	 * 
	 * @param columnCount	The total number of columns in the corpus
	 * @return				The inverse frequency for unseen words
	 */
	private double getZeroFrequencyIDF(int columnCount) {
		return Math.log((double)columnCount);
	}

	/**
	 * Gets the inverse frequency associated with a word.
	 * 
	 * @param word	The word
	 * @return		The inverse frequency
	 */
	public double getInverseFrequency(String word) {
		double inverseFrequency = 0;
		if (inverseFrequencies.containsKey(word)) {
			inverseFrequency = inverseFrequencies.get(word);
		} else {
			inverseFrequency = inverseFrequencies.get(UNSEEN_WORD);
		}
		return inverseFrequency;
	}

	/**
	 * Loads an InverseColumnFrequency table from a file.
	 * 
	 * @param file	The input file
	 * @return		The new InverseColumnFrequency object
	 */
	public static InverseColumnFrequency readFromFile(File file) {
		WordScoreReader reader = new WordScoreReader(file);
		reader.read();
		return new InverseColumnFrequency(reader.getWordScores());
	}

}
