import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * The FusionColumnRecognizer combines the result of multiple 
 * ColumnRecognizers.
 * 
 * @author Simon
 *
 */
public class FusionColumnRecognizer extends ColumnRecognizer {
	/**
	 * The confidence threshold
	 */	
	double threshold = 0.7;
	
	/**
	 * The component recognizers
	 */
	List<ColumnRecognizer> componentRecognizers = new ArrayList<ColumnRecognizer>();
	
	/**
	 * Constructs the FusionColumnRecognizer.
	 * 
	 * @param id			A unique name for the recognizer instance
	 * @param threshold		Its confidence threshold
	 */
	public FusionColumnRecognizer(String id, double threshold) {
		super(id);
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see ColumnRecognizer#computeScoredCandidates()
	 */
	@Override
	public List<ColumnConceptCandidate> computeScoredCandidates() {
		Map<ColumnConcept, Double> candidateMap = sumAllCandidates();
		List<ColumnConceptCandidate> candidates = buildCandidateListFromMap(candidateMap);
		applyThreshold(candidates, threshold);
		
		return candidates;
	}
	
	/**
	 * Computes the candidates and scores from the component recognizers and 
	 * adds up the scores.
	 * 
	 * @return		The map from column-concepts to scores
	 */
	private Map<ColumnConcept, Double> sumAllCandidates() {
		Map<ColumnConcept, Double> candidateMap = new HashMap<ColumnConcept, Double>();
		Iterator<ColumnRecognizer> it = componentRecognizers.iterator();
		while (it.hasNext()) {
			List<ColumnConceptCandidate> newCandidates = it.next().computeScoredCandidates();
			sumCandidates(candidateMap, newCandidates);
		}
		return candidateMap;
	}

	/**
	 * Transfers column-concept candidates from map to list
	 * 
	 * @param candidateMap	The map of candidates and scores
	 * @return 				The list of scored candidates
	 */
	private List<ColumnConceptCandidate> buildCandidateListFromMap(
			Map<ColumnConcept, Double> candidateMap) {
		List<ColumnConceptCandidate> candidates = new ArrayList<ColumnConceptCandidate>();
		Set<ColumnConcept> columnConcepts = candidateMap.keySet();
		Iterator<ColumnConcept> itColumnConcept = columnConcepts.iterator();
		while (itColumnConcept.hasNext()) {
			ColumnConcept columnConcept = itColumnConcept.next();
			ColumnConceptCandidate candidate 
				= new ColumnConceptCandidate(columnConcept.columnNumber, columnConcept.conceptID);
			candidate.setScore(candidateMap.get(columnConcept));
			candidates.add(candidate);
		}
		return candidates;
	}

	/**
	 * Removes candidates that fall below the minimal score.
	 * 
	 * @param candidates	The list of scored column-concept candidates
	 * @param minimalScore	The threshold
	 */
	private void applyThreshold(List<ColumnConceptCandidate> candidates,
			double minimalScore) {
		Iterator <ColumnConceptCandidate> it = candidates.iterator();
		while (it.hasNext()) {
			if (it.next().getScore() < threshold) {
				it.remove();
			}
		}
		
	}

	/**
	 * Combines candidates by summing their scores.
	 * 
	 * @param candidateMap	The map from column-concepts to scores
	 * @param newCandidates	A list of scored candidates to add
	 */
	private void sumCandidates(
			Map<ColumnConcept, Double> candidateMap,
			List<ColumnConceptCandidate> newCandidates) {
		Iterator<ColumnConceptCandidate> it = newCandidates.iterator();
		while (it.hasNext()) {
			ColumnConceptCandidate candidate = it.next();
			ColumnConcept columnConcept = candidate.getColumnConcept();
			if (candidateMap.containsKey(columnConcept)) {
				double oldScore = candidateMap.get(columnConcept);
				double newScore = oldScore + candidate.getScore();
				candidateMap.put(columnConcept, newScore);
			} else {
				candidateMap.put(columnConcept, candidate.getScore());
			}
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
