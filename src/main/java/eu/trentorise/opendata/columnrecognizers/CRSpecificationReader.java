package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;

// TODO The description below is out of date.

/**
 * 
 * The CRSpecificationReader reads column recognizer specification files.
 * The format is indicated below.
 * 
 
   # This is a column recognizer specification file.
   #
   # It can include comment lines, blank lines, and specification lines.
   #
   # Comment lines start with the hash symbol.
   #
   # Specification lines consist of 
   # 	conceptID	type	model_string
   # according to one of the following patterns:
   #
   # <conceptID> REGEX <regex>
   # <conceptID> VALUE_SET <file_path>
   #
   # Each specification line produces a column recognizer. 
   #

   # Restaurant
   2001 REGEX		((trattoria)|(ristorante)|(ost[ae]ria)) [a-z]+
   
   # Town (comune)
   4001 VALUE_SET	comune.txt
   
 *
 * @author Simon
 *
 */
/**
 * @author Simon
 *
 */
public class CRSpecificationReader extends SyntaxPatternLineReader {
	final static String LINE_SYNTAX = "(\\w+)\\s+([0-9]+)\\s+(\\w+)\\s*(.*)";
	final static int RECOGNIZER_ID_POSITION = 1;
	final static int CONCEPT_ID_POSITION = 2;
	final static int TYPE_POSITION = 3;
	final static int MODEL_POSITION = 4;
	
	/**
	 * List of directories containing recognizer models.
	 */
	List<File> modelDirectories = null;
	
	CompositeColumnRecognizer compositeCR = null;
	Table table = null;
	RowTable sample = null;
	
	/**
	 * Constructs the reader
	 * 
	 * @param file				The specification file
	 * @param modelDirectories 	List of model directories (can be null)
	 * @param compositeCR		The composite recognizer to which the CRs will be attached
	 * @param table				The data table (or a large sample)
	 * @param sample			A smaller sample of rows from the table
	 */
	public CRSpecificationReader(
			File file, 
			List<File> modelDirectories, 
			CompositeColumnRecognizer compositeCR,
			Table table, 
			RowTable sample) {
		super(file, LINE_SYNTAX);
		init(modelDirectories, compositeCR, table, sample);
	}
		
	/**
	 * Constructs the reader
	 * 
	 * @param stream			An input stream to the specification file
	 * @param modelDirectories 	List of model directories (can be null)
	 * @param compositeCR		The composite recognizer to which the CRs will be attached
	 * @param table				The data table (or a large sample)
	 * @param sample			A smaller sample of rows from the table
	 */
	public CRSpecificationReader(
			InputStream stream,
			List<File> modelDirectories, 
			CompositeColumnRecognizer compositeCR,
			Table table, 
			RowTable sample) {
		super(stream, LINE_SYNTAX);
		init(modelDirectories, compositeCR, table, sample);
	}
	
	/**
	 * Initializes the CRSpecificationReader.
	 * 
	 * @param modelDirectories 	List of model directories (can be null)
	 * @param compositeCR		The composite recognizer to which the CRs will be attached
	 * @param table				The data table (or a large sample)
	 * @param sample			A smaller sample of rows from the table
	 */
	private void init(
			List<File> modelDirectories, 
			CompositeColumnRecognizer compositeCR,
			Table table, 
			RowTable sample) {
		this.modelDirectories = modelDirectories;
		this.compositeCR = compositeCR;
		this.table = table;
		this.sample = sample;
	}


	/**
	 * Processes the matched line.
	 */
	@Override
	protected void processMatch(Matcher matcher) {
		// TODO We should have some error handling here
		String recognizerID = matcher.group(RECOGNIZER_ID_POSITION);
		long conceptID = Long.parseLong(matcher.group(CONCEPT_ID_POSITION));
		String type = matcher.group(TYPE_POSITION);
		String model = matcher.group(MODEL_POSITION);
		
		ColumnRecognizer newRecognizer 
			= ColumnRecognizerFactory.makeRecognizer(recognizerID, 
					conceptID, 
					type, 
					model, 
					modelDirectories,
					table, 
					sample);
		compositeCR.add(newRecognizer); 		
	}

}
