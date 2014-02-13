import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * @author Simon
 *
 */
public class WordScoreReader extends SyntaxPatternLineReader {
	/**
	 * The line syntax: 
	 * word word_score
	 */
	private static final String LINE_SYNTAX = "(\\w+)\\W*([0-9.,e+-]+)";
	
	/**
	 * The position of the word field
	 */
	final static int WORD_POSITION = 1;
	
	/**
	 * The position of the score field
	 */
	final static int SCORE_POSITION = 2;
	
	/**
	 * The map of word scores
	 */
	private Map<String, Double> wordScores = new HashMap<String, Double>();
	
	/**
	 * @param file 	The input file
	 */
	public WordScoreReader(File file) {
		super(file, LINE_SYNTAX);
	}

	@Override
	protected void processMatch(Matcher matcher) {
		String word = matcher.group(WORD_POSITION);
		double score = Double.parseDouble(matcher.group(SCORE_POSITION));
		wordScores.put(word, score);
	}

	/**
	 * Retrieves the word scores. Call this after calling the read method.
	 * 
	 * @return
	 */
	public Map<String, Double> getWordScores() {
		return wordScores;
	}
	
}
