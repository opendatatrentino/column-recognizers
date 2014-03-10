package eu.trentorise.opendata.columnrecognizers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
	 * The component recognizers by name
	 */
	Map<String, ColumnRecognizer> componentRecognizersByName 
		= new HashMap<String, ColumnRecognizer>();
	
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
	 * @param component		The component recognizer
	 */
	public void add(ColumnRecognizer component) {
		componentRecognizers.add(component);
		componentRecognizersByName.put(component.getId(), component);
	}

	/**
	 * Retrieves a component recognizer and removes it.
	 * 
	 * @param string	The ID of the component recognizer
	 * @return			The component recognizer
	 */
	public ColumnRecognizer detach(String string) {
		ColumnRecognizer component = get(string);
		assert(component != null);
		remove(component);
		return component;
	}

	/**
	 * Removes a component recognizer.
	 * 
	 * @param component		The component recognizer
	 */
	public void remove(ColumnRecognizer component) {
		componentRecognizers.remove(component);		
		componentRecognizersByName.remove(component.getId());
	}

	public ColumnRecognizer get(String string) {
		return componentRecognizersByName.get(string);
	}

}
