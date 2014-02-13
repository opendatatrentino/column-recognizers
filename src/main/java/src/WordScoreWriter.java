import java.io.File;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * The WordScoreWriter writes out pairs of (String, real number) to a file.
 * 
 * @author Simon
 *
 */
public class WordScoreWriter extends LineWriter {
	/**
	 * The (string, value) pairs to be written
	 */
	Map<String, Double> wordScores = null;
	
	/**
	 * The iterator tracking the current entry
	 */
	Iterator<Entry<String, Double>> it = null;

	/**
	 * Constructs the writer.
	 * 
	 * @param file	The output file
	 */
	public WordScoreWriter(File file, Map<String, Double> wordScores) {
		super(file);
		this.wordScores = wordScores;
		it = wordScores.entrySet().iterator();
	}

	/* (non-Javadoc)
	 * @see LineWriter#hasNext()
	 */
	@Override
	protected boolean hasNext() {
		return it.hasNext();
	}

	/* (non-Javadoc)
	 * @see LineWriter#next()
	 */
	@Override
	protected String next() {
		Entry<String, Double> wordScore = it.next();
		String word = wordScore.getKey();
		double score = wordScore.getValue();
		return String.format(Locale.US, "%s %f", word, score);
	}

}
