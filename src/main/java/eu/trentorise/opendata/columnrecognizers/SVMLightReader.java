package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * The SVMLightReader reads a text file in the SVM-Light format.
 * This code has not been tested.
 * We probably don't need this class - we just need to read predictions files.
 * 
 * @author Simon
 *
 */
public class SVMLightReader extends SyntaxPatternLineReader {
	/**
	 * Defines the format of the SVM-Light file
	 * A line in the SVM-Light format represents a single example or prediction
	 * and can look like this:
	 * 
	 * 1 1:0.000000 2:1.000000 # Vivifiemme ID
	 * 
	 * The first field is the label, subsequent fields specify features
	 * (feature_number:value). A comment can be added, preceded by a hash
	 * symbol.
	 * 
	 */
	private static final String LINE_SYNTAX 
		= "([-+.0-9eE]+)\\w*(?:([0-9]+):([-+.0-9eE]+))*\\w*(?:#.*)?";
	
	/**
	 * The position of the label field
	 */
	private static final int LABEL_POSITION = 1;
	
	/**
	 * The position where the features start
	 */
	private static final int FIRST_FEATURE_POSITION = 2;	
	
	/**
	 * Vector of the values read: Element 0 is the label, elements 1, 2, ... 
	 * correspond to features 1, 2, ...
	 */
	private List<Double> vector = new ArrayList<Double>();
	
	/**
	 * Constructs the reader.
	 * 
	 * @param file		The input file
	 */
	public SVMLightReader(File file) {
		super(file, LINE_SYNTAX);
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.SyntaxPatternLineReader#processMatch(java.util.regex.Matcher)
	 */
	@Override
	protected void processMatch(Matcher matcher) {
		double label = Double.parseDouble(matcher.group(LABEL_POSITION));
		vector.add(label);
		int groupNumber = FIRST_FEATURE_POSITION;
		while (groupNumber < matcher.groupCount()) { 
			int featureNumber = Integer.parseInt(matcher.group(groupNumber++));
			double featureValue = Double.parseDouble(matcher.group(groupNumber++));
			while (vector.size() < featureNumber) {
				vector.add((double) 0);
			}
			vector.add(featureValue);
		}
	}

	/**
	 * Returns the (dense) vector of values read
	 * 
	 * @return	The vector
	 */
	List<Double> getDenseVector() {
		return vector;
	}
}
