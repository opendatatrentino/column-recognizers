package eu.trentorise.opendata.columnrecognizers;

/**
 * @author Simon
 *
 */
public class CRStringUtils {
	/**
	 * Counts the number of times a character occurs in a string.
	 * 
	 * @param string		The string
	 * @param character		The character to be found
	 * @return				The number of occurrences
	 */
	public static int countOccurrences(String string, char character) {
		int count = 0;
		for(int i = 0; i < string.length(); i++)
		    if(string.charAt(i) == character)
		        count++;
		return count;
	}
	
	/**
	 * Normalizes the text is a row or field.
	 * 
	 * @param text	The text to normalize
	 * @return		The normalized version
	 */
	public static String normalize(String text) {
		return text.toLowerCase();
	}
}
