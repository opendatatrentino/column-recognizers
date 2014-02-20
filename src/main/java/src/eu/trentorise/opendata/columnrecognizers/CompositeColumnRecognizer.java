package eu.trentorise.opendata.columnrecognizers;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The CompositeColumnRecognizer combines the results of multiple 
 * ColumnRecognizers.
 * 
 * @author Simon
 *
 */
public class CompositeColumnRecognizer extends ColumnRecognizer {
	/**
	 * The component recognizers
	 */
	List<ColumnRecognizer> componentRecognizers = new ArrayList<ColumnRecognizer>();
	
	/**
	 * Constructs the CompositeColumnRecognizer.
	 * 
	 * @param id			A unique name for the recognizer instance
	 */
	public CompositeColumnRecognizer(String id) {
		super(id);
	}

	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		Iterator<ColumnRecognizer> it = componentRecognizers.iterator();
		while (it.hasNext()) {
			it.next().computeScoredCandidates(candidates);
		}
	}

	/**
	 * Installs a component recognizer.
	 * 
	 * @param componentRecognizer	The component recognizer
	 */
	public void add(ColumnRecognizer componentRecognizer) {
		componentRecognizers.add(componentRecognizer);
	}

}
