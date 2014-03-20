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
	 * @param recognizerID 
	 * 
	 * @param conceptID	The knowledge base concept ID
	 * @param type		The recognizer type (REGEX, VALUE_SET, ...)
	 * @param model		The model data (a regex, a file name...)
	 * @param table		The entire data table (or largest possible sample)
	 * @param sample	A small sample of the data
	 * @return			The new column recognizer
	 */
	public static ColumnRecognizer makeRecognizer(String recognizerID, 
			long conceptID, 
			String type, 
			String model,
			Table table,
			RowTable sample) {
		ColumnRecognizer recognizer = null;
		InverseColumnFrequency inverseFrequencies = null;
		
		if (type.equals("REGEX")) {
			recognizer = new RegExColumnRecognizer(recognizerID, conceptID, model, sample);
		} else if (type.equals("REGEX_S")) {
			RegExColumnRecognizer regExRecognizer 
				= new RegExColumnRecognizer(recognizerID, conceptID, model, sample);
			regExRecognizer.beCaseSensitive();
			recognizer = regExRecognizer;
		} else if (type.equals("VALUE_SET")) {
			File modelFile = new File(model);
			recognizer = new ValueSetCR(recognizerID, 
					conceptID, 
					RowTable.loadValueSet(modelFile), 
					table);
		} else if (type.equals("TF_IDF")) {
			if (inverseFrequencies == null) {
				File idfFile = new File(INVERSE_FREQUENCIES_PATH);
				inverseFrequencies = InverseColumnFrequency.readFromFile(idfFile);
			}
			File modelFile = new File(model);
			recognizer = new TFIDFColumnRecognizer(recognizerID,
					conceptID, 
					TFIDFVector.readFromFile(modelFile),
					inverseFrequencies,
					table);
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
