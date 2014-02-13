
/**
 * The ItalianRestaurantCR is a heuristic column recognizer for restaurant
 * names in Italian.
 * 
 * Deprecated - use specification files instead.
 * 
 * @author Simon
 *
 */
public class ItalianRestaurantCR extends RegExColumnRecognizer {
	final static String ITALIAN_RESTAURANT_REG_EX
		= "((trattoria)|(ristorante)|(ost[ae]ria)) [a-z]+";
	
	public ItalianRestaurantCR(long itsConceptID, RowTable itsData) {
		super(itsConceptID, ITALIAN_RESTAURANT_REG_EX, itsData);
	}
	
}
