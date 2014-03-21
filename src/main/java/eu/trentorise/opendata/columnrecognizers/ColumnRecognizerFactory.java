package eu.trentorise.opendata.columnrecognizers;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ColumnRecognizerFactory builds ColumnRecognizer objects.
 * 
 * @author Simon
 *
 */
public class ColumnRecognizerFactory {
	private static final String INVERSE_FREQUENCIES_PATH = "inverse-frequencies.txt";
	
	/**
	 * Constructs a ColumnRecognizer. The type indicates the class of the 
	 * recognizer. The model string can contain the information needed to build
	 * the object (a regular expression, for example) or it can point to a 
	 * model file.
	 * 
	 * @param recognizerID 	The identifier of the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param type			The recognizer type (REGEX, VALUE_SET, ...)
	 * @param model			The model data (a regex, a file name...)
	 * @param table			The entire data table (or largest possible sample)
	 * @param sample		A small sample of the data
	 * @return				The new column recognizer
	 */
	public static ColumnRecognizer makeRecognizer(String recognizerID, 
			long conceptID, 
			String type, 
			String model,
			Table table,
			RowTable sample) {
		ColumnRecognizer recognizer = null;
		
		if (type.equals("REGEX")) {
			recognizer = makeRegExRecognizer(recognizerID, conceptID, model, sample, false);
		} else if (type.equals("REGEX_S")) {
			recognizer = makeRegExRecognizer(recognizerID, conceptID, model, sample, true);
		} else if (type.equals("VALUE_SET")) {
			recognizer = makeValueSetRecognizer(recognizerID, conceptID, model, table);
		} else if (type.equals("TF_IDF")) {
			recognizer = makeTFIDFRecognizer(recognizerID, conceptID, model, table);
		} else if (type.equals("SUM_THRESHOLD")) {
			double threshold = Double.parseDouble(model);
			recognizer = new SumThresholdFusionCR(recognizerID, threshold);
		} else if (type.equals("ONE_BEST")) {
			recognizer = new OneBestFusionCR(recognizerID);
		} else if (type.equals("SVM")) {
			// TODO check for syntax error
			String[] fields = model.split("\\s*;\\s*");
			String modelPath = fields[0];
			String[] inputRecognizerIDs = fields[1].split("\\s*,\\s*");
			Set<String> inputRecognizers = new HashSet<String>();
			inputRecognizers.addAll(Arrays.asList(inputRecognizerIDs));
			recognizer = new ClassifierFusionCR(
					recognizerID, 
					conceptID, 
					table, 
					new File(modelPath), 
					inputRecognizers);
		} else if (type.equals("HEADER_NLP")) {
			recognizer = new HeaderNLPCR(recognizerID, table);
		} else if (type.equals("HEADER_REGEX")) {
			String[] fields = model.split("\\s*;\\s*");
			double score = Double.parseDouble(fields[0]);
			String regEx = fields[1];
			recognizer = new HeaderRegExCR(recognizerID, conceptID, regEx, score, table);
		}
		// TODO signal error for unknown type
		
		return recognizer;
	}
	
	/**
	 * Constructs a ValueSetCR.
	 * 
	 * @param recognizerID 	The identifier of the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param model			The model data (a regex, a file name...)
	 * @param table			The entire data table (or largest possible sample)
	 * @return				The recognizer
	 */
	private static ColumnRecognizer makeValueSetRecognizer(
			String recognizerID,
			long conceptID, 
			String model, 
			Table table) {
		File modelFile = new File(model);
		return new ValueSetCR(recognizerID, 
				conceptID, 
				RowTable.loadValueSet(modelFile), 
				table);
	}

	/**
	 * Constructs a TFIDFColumnRecognizer.
	 * 
	 * @param recognizerID 	The identifier of the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param model			The model data (a regex, a file name...)
	 * @param table			The entire data table (or largest possible sample)
	 * @return				The recognizer
	 */
	private static ColumnRecognizer makeTFIDFRecognizer(
			String recognizerID,
			long conceptID, 
			String model, 
			Table table) {
		InverseColumnFrequency inverseFrequencies = null;
		
		if (inverseFrequencies == null) {
			File idfFile = new File(INVERSE_FREQUENCIES_PATH);
			inverseFrequencies = InverseColumnFrequency.readFromFile(idfFile);
		}
		File modelFile = new File(model);
		return new TFIDFColumnRecognizer(recognizerID,
				conceptID, 
				TFIDFVector.readFromFile(modelFile),
				inverseFrequencies,
				table);
	}

	/**
	 * Constructs a RegExColumnRecognizer.
	 * 
	 * @param recognizerID 	The identifier of the recognizer instance
	 * @param conceptID		The knowledge base concept ID
	 * @param model			The model data (a regex, a file name...)
	 * @param sample		A small sample of the data
	 * @param caseSensitive	True for case sensitive matching
	 * @return				The recognizer
	 */
	public static ColumnRecognizer makeRegExRecognizer(
			String recognizerID, 
			long conceptID, 
			String model,
			RowTable sample,
			boolean caseSensitive) {
		RegExColumnRecognizer regExRecognizer 
			= new RegExColumnRecognizer(recognizerID, conceptID, model, sample);
		if (caseSensitive) {
			regExRecognizer.beCaseSensitive();
		}
		return regExRecognizer;
	}
	
	/**
	 * Constructs ColumnRecognizers from a specification file and installs them
	 * in the CompositeColumnRecognizer. 
	 * 
	 * @param compositeCR		The composite CR that will hold the recognizers
	 * @param specificationFile	The file that specifies the recognizers
	 * @param table				The entire data table (or largest possible sample)
	 * @param sample			A small sample of the data
	 */
	public static void attachRecognizers(CompositeColumnRecognizer compositeCR, 
			File specificationFile,
			Table table,
			RowTable sample) {
		CRSpecificationReader reader 
			= new CRSpecificationReader(specificationFile, compositeCR, table, sample);
		reader.read();
	}	

}
