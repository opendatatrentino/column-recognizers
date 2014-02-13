
/**
 * @author Simon
 * 
 * Deprecated - use specification files instead.
 * 
 */
public class ItalianStreetAddressCR extends RegExColumnRecognizer {
	final static String ITALIAN_STREET_ADDRESS_REG_EX
		= "((via)|(viale)|(piazza)|(piazzetta)) [ a-z]+[;]?[0-9]+";

	public ItalianStreetAddressCR(long itsConceptID, RowTable itsData) {
		super(itsConceptID, ITALIAN_STREET_ADDRESS_REG_EX, itsData);
	}

}
