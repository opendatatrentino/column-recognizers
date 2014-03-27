package eu.trentorise.opendata.columnrecognizers;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



/**
 * The LineReader is an abstract class for reading and processing lines
 * from a file.
 * 
 * @author Simon
 *
 */
public abstract class LineReader {
	/**
	 * Character marking a comment line
	 */
	final static char COMMENT_CHAR = '#';

	/**
	 * The input file
	 */
//	private File file = null;
	
	/**
	 * The input stream
	 */
	private InputStream inputStream = null;
	
	/**
	 * Set to true to ignore comment lines
	 */
	private boolean ignoreCommentLines = false;
	
	/**
	 * Set to true to ignore empty lines
	 */
	private boolean ignoreBlankLines = false;
	
	/**
	 * Set to true to ignore leading and trailing whitespace
	 */
	private boolean trimLines = false;

	/**
	 * Creates the LineReader.
	 * 
	 * @param file	The input file
	 */
	public LineReader(File file) {
		super();
//		this.file = file;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates the LineReader.
	 * 
	 * @param file					The input file
	 * @param ignoreCommentLines	Ignore comments
	 * @param ignoreBlankLines		Ignore empty lines
	 * @param trimLines				Ignore leading and trailing space
	 */
	public LineReader(File file, 
			boolean ignoreCommentLines,
			boolean ignoreBlankLines, 
			boolean trimLines) {
		super();
//		this.file = file;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {		
			e.printStackTrace();
		}
		this.ignoreCommentLines = ignoreCommentLines;
		this.ignoreBlankLines = ignoreBlankLines;
		this.trimLines = trimLines;
	}

	/**
	 * Creates the LineReader.
	 * 
	 * @param stream				The input stream
	 * @param ignoreCommentLines	Ignore comments
	 * @param ignoreBlankLines		Ignore empty lines
	 * @param trimLines				Ignore leading and trailing space
	 */
	public LineReader(InputStream stream, 
			boolean ignoreCommentLines,
			boolean ignoreBlankLines, 
			boolean trimLines) {
		inputStream = stream;
		this.ignoreCommentLines = ignoreCommentLines;
		this.ignoreBlankLines = ignoreBlankLines;
		this.trimLines = trimLines;
	}

	/**
	 * Constructs the LineReader. 
	 * 
	 * @param stream 	The input stream
	 */
	public LineReader(InputStream stream) {
		inputStream = stream;
	}

	/**
	 * Processes the file, calling processLine for each line.
	 */
	public void read() {
		BufferedReader br = null;
		String line = "";
	 
		try {
			br = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));
//								new FileInputStream(file), "UTF8"));
			while ((line = br.readLine()) != null) {
				handleLine(line);
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
	}
	
	private void handleLine(String line) {
		if (trimLines) {
			line = line.trim();
		}
		boolean ignoreLine 
			= ignoreBlankLines && line.isEmpty() 
			|| ignoreCommentLines && (line.charAt(0) == COMMENT_CHAR); 
		if (!ignoreLine) {
			processLine(line);
		}		
	}

	/**
	 * Processes a line. Subclasses must implement this method.
	 * 
	 * @param line	The line of text
	 */
	protected abstract void processLine(String line);
}
