package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The VectorReader reads a text file where each line contains a floating-point
 * number.
 * 
 * @author Simon
 *
 */
public class VectorReader extends LineReader {
	private List<Double> vector = new ArrayList<Double>();

	/**
	 * @param file
	 */
	public VectorReader(File file) {
		super(file, /* ignoreCommentLines: */ true, 
				/* ignoreBlankLines: */ true, 
				/* trimLines */ true);
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.LineReader#processLine(java.lang.String)
	 */
	@Override
	protected void processLine(String line) {
		vector.add(Double.parseDouble(line));
	}
	
	/**
	 * Gets the vector read.
	 * 
	 * @return	The vector
	 */
	List<Double> getVector() {
		return vector;
	}

	/**
	 * Reads a vector from a file and returns it.
	 * 
	 * @param file	The input file
	 * @return		The vector
	 */
	public static List<Double> read(File file) {
		VectorReader reader = new VectorReader(file);
		reader.read();
		return reader.getVector();
	}

}
