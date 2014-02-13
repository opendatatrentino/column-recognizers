import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The SyntaxPatternLineReader reads input lines and matches them to a 
 * syntax pattern. The pattern should contain parentheses to capture the groups
 * that define the content fields of the input. 
 * 
 * @author Simon
 *
 */
public abstract class SyntaxPatternLineReader extends LineReader {
	/**
	 * The regular expression that specifies the line syntax
	 */
	private Pattern syntaxPattern = null;

	/**
	 * Constructs the SyntaxPatternLineReader.
	 * 
	 * @param file				The input file
	 * @param syntaxPattern		A regular expression that specifies the line syntax
	 */
	public SyntaxPatternLineReader(File file, String syntaxPattern) {
		super(file, /* ignoreCommentLines: */ true, /* ignoreBlankLines: true */ true,
				/* trimLines: */ true);
		this.syntaxPattern = Pattern.compile(syntaxPattern);
	}

	/* (non-Javadoc)
	 * @see LineReader#processLine(java.lang.String)
	 */
	@Override
	protected void processLine(String line) {
		Matcher matcher = syntaxPattern.matcher(line);
		if (matcher.matches()) {
			processMatch(matcher);
		}
		// TODO We should have some handling of non-matches
	}
	
	/**
	 * Processes the matched line. Subclasses need to implement this method and
	 * call matcher.group to get the content fields of the input line.
	 * 
	 * @param matcher	The matcher
	 */
	protected abstract void processMatch(Matcher matcher);

}
