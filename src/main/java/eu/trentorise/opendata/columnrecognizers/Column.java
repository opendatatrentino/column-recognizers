package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.trentorise.opendata.nlprise.DataTypeGuess.Datatype;

/**
 * The Column class represents a table column.
 *
 * @author Simon
 *
 */
public class Column {
	/**
	 * The number of possible column data types
	 */
	private static final int NUMBER_OF_DATATYPES = Datatype.values().length;
	
	/**
	 * The fields in the column
	 */
	private List<String> fields = null;
	
	/**
	 * The column features are cached for efficiency
	 */
	private List<Double> cachedFeatures = null;
	
	/**
	 * The datatype of the column
	 */
	private Datatype columnType = null;
	
	/**
	 * Constructs the column.
	 */
	public Column() {
		fields = new ArrayList<String>();
	}
	
	/**
	 * Constructs the column.
	 * 
	 * @param fields	The column data
	 */
	public Column(List<String> fields) {
		this.fields = fields;
	}

	/**
	 * Gets the set of values that exist in a single column.
	 * 
	 * @return	The value set
	 */
	public Set<String> getValueSet() {
		Set<String> valueSet = new HashSet<String>();
		
		Iterator<String> it = getFieldIterator();
		while (it.hasNext()) {
			valueSet.add(it.next());
		}
		
		return valueSet;
	}
	
	/**
	 * Gets the set of values in a column.
	 * The values are normalized.
	 * 
	 * @return	The normalized value sets
	 */
	private Set<String> getNormalizedValueSet() {
		Set<String> valueSet = new HashSet<String>();
		Iterator<String> it = getFieldIterator();
		while (it.hasNext()) {
			valueSet.add(CRStringUtils.normalize(it.next()));
		}
		
		return valueSet;
	}

	/**
	 * Return the uniqueness, a value 0 < u <= 1 measuring the fraction of
	 * unique values in a column.
	 * 
	 * @return		The uniqueness value
	 */
	public double getUniqueness() {
		return ((double)getValueSet().size()) / fields.size();
	}

	/**
	 * Outputs the normalized value set to a file.
	 * 
	 * @param file	The output file
	 */
	public void writeNormalizedValueSetToFile(File file) {
		Set<String> values = getNormalizedValueSet();		
		RowTable.writeStringsToFile(values.iterator(), file);
	}

	/**
	 * Adds a field at the bottom of the column.
	 * 
	 * @param field		The contents of the field
	 */
	public void appendField(String field) {
		fields.add(field);
	}
	
	/**
	 * Returns an iterator to the fields in the column.
	 * 
	 * @return	The iterator
	 */
	public Iterator<String> getFieldIterator() {
		return fields.iterator();
	}
	
	/**
	 * Extracts a list of the words occurring in the table, preserving their
	 * order.
	 * 
	 * @return	The word list
	 */
	public List<String> extractWords() {
		List<String> words = new ArrayList<String>();
		Iterator<String> it = getFieldIterator();
		while (it.hasNext()) {
			String row = it.next();
			String[] rowWords = row.split("\\W+");
			for (int i = 0; i < rowWords.length; i++) {
				String word = CRStringUtils.normalize(rowWords[i]);
				if (!word.isEmpty()) {
					words.add(word);
				}
			}
		}
		return words;
	}
	
	/**
	 * Gets the set of terms in the table
	 * 
	 * @return	The set of terms
	 */
	public Set<String> extractWordSet() {
		// Suppress the shortest words
		final int MINIMAL_WORD_LENGTH = 3;
	
		Set<String> words = new HashSet<String>();
		Iterator<String> it = getFieldIterator();
		while (it.hasNext()) {
			String row = it.next();
			String[] rowWords = row.split("\\W+");
			for (int i = 0; i < rowWords.length; i++) {
				String word = CRStringUtils.normalize(rowWords[i]);
				if (word.length() >= MINIMAL_WORD_LENGTH) {
					words.add(word);
				}
			}
		}
		return words;
	}

	/**
	 * Computes the frequencies of words in the table.
	 * 
	 * @return	The word frequencies
	 */
	public Map<String, Integer> computeWordFrequencies () {
		// Suppress the shortest words
		final int MINIMAL_WORD_LENGTH = 3;
		
		Map<String, Integer> frequencies = new HashMap<String, Integer>();
		List<String> words = extractWords();
		Iterator<String> it = words.iterator();
		while (it.hasNext()) {
			String word = it.next();
			if (word.length() >= MINIMAL_WORD_LENGTH) {
				if (frequencies.containsKey(word)) {
					frequencies.put(word, frequencies.get(word) + 1);
				} else {
					frequencies.put(word, 1);
				}
			}
		}
		return frequencies;
	}

	/**
	 * Returns the number of fields of this column.
	 * 
	 * @return	The column size
	 */
	public int size() {
		return fields.size();
	}

	/**
	 * Returns the field at the given zero-based row number.
	 * 
	 * @param index	The row number
	 * @return		The field contents
	 */
	public String getFieldAt(int index) {
		return fields.get(index);
	}

	/**
	 * Retrieves the contents of the column.
	 * 
	 * @return	The column data
	 */
	public List<String> getContents() {
		return fields;
	}

	/**
	 * Gets the vector of column features.
	 * Column features are numbers in [0, 1] that describe some aspect of the
	 * column and that can be used as input to classifiers. 
	 * An example of a column feature is uniqueness (the ratio of unique
	 * values to total number of rows), which helps discriminate 'name' fields
	 * from 'type of' fields.
	 * 
	 * @return	The column features
	 */
	public List<Double> getFeatures() {
		if (cachedFeatures == null) {
			cachedFeatures = new ArrayList<Double>();
			cachedFeatures.add(getUniqueness());
			cachedFeatures.addAll(computeTypeFeatures());
		}
		return cachedFeatures;
	}

	/**
	 * Calculates the column features indicating type. The number of features
	 * equals the number of types. All features are zero except the one 
	 * corresponding to the column type; this has the value one.
	 * 
	 * @return		The feature list
	 */
	private List<Double> computeTypeFeatures() {
		List<Double> typeFeatures = new ArrayList<Double>();
		Datatype columnType = getType();
		for (Datatype type : Datatype.values()) {
			typeFeatures.add(columnType == type ? 1. : 0.);
		}
		return typeFeatures;
	}

	/**
	 * Gets the number of possible column data types.
	 * 
	 * @return		The number of types
	 */
	public int getNumberOfDatatypes() {
		return NUMBER_OF_DATATYPES;
	}

	/**
	 * Get the datatype of this column
	 * 
	 * @return
	 */
	public Datatype getType() {
		if (columnType == null) {
			columnType = TypeDetector.guessType(this);
		}
		return columnType;
	}


}
