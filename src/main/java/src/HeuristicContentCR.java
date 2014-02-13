import java.io.File;
import java.util.Set;

/**
 * Column recognizer that applies heuristics to table content.
 * 
 * Deprecated - use specification files instead.
 * 
 * @author Simon
 *
 */
public class HeuristicContentCR extends FusionColumnRecognizer {
	/**
	 * The knowledge base concept ID for Restaurant
	 */
	static final long RESTAURANT_CONCEPT_ID = 2001;
	
	/**
	 * The knowledge base concept ID for Street address
	 */
	static final long STREET_ADDRESS_CONCEPT_ID = 3001;

	/**
	 * The knowledge base concept ID for Town
	 */
	static final long TOWN_CONCEPT_ID = 4001;

	/**
	 * The knowledge base concept ID for Frazione
	 */
	static final long FRAZIONE_CONCEPT_ID = 5001;
	
	/**
	 * The confidence threshold for column-concept candidates
	 * Since the HeuristContentCR will be used as a component recognizer, the 
	 * threshold is set to zero (or a very low value) so as not to lose any
	 * information.
	 */
	static final double MINIMAL_SCORE = 0;
	
	/**
	 * The path to the file containing names of towns
	 */
	static final String TOWNS_FILE_PATH = "comune.txt";
	
	/**
	 * The path to the file containing names of frazione
	 */
	static final String FRAZIONI_FILE_PATH = "frazione.txt";

	/**
	 * Creates the column recognizer
	 * 
	 * @param itsData	The sample of table rows
	 */
	public HeuristicContentCR(RowTable itsData) {
		super(MINIMAL_SCORE);
		RowTable rowSample = itsData.extractSample();

		add(new ItalianRestaurantCR(RESTAURANT_CONCEPT_ID, rowSample));
		add(new ItalianStreetAddressCR(STREET_ADDRESS_CONCEPT_ID, rowSample));
		
		Set<String> towns = RowTable.loadValueSet(new File(TOWNS_FILE_PATH));
		add(new ValueSetCR(TOWN_CONCEPT_ID, towns, itsData));
		Set<String> frazioni = RowTable.loadValueSet(new File(FRAZIONI_FILE_PATH));
		add(new ValueSetCR(FRAZIONE_CONCEPT_ID, frazioni, itsData));
	}
}
