package eu.trentorise.opendata.columnrecognizers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Combines candidates by summing their scores, replacing the input 
 * candidates (many per column-concept pair) with the summed candidates
 * (one per column-concept pair).
 * 
 * @author Simon
 *
 */
public class SumThresholdFusionCR extends ColumnRecognizer {
	/**
	 * The confidence threshold
	 */	
	private double threshold = 0.7;

	/**
	 * Constructs the SumThresholdFusionCR.
	 * 
	 * @param id	A unique name for the recognizer instance
	 */
	public SumThresholdFusionCR(String id, double threshold) {
		super(id);
		this.threshold = threshold;
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.ColumnRecognizer#computeScoredCandidates(java.util.List)
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		Map<ColumnConcept, Double> candidateMap = sumCandidates(candidates);
		buildCandidateListFromMap(candidateMap, candidates);
		applyThreshold(candidates);
	}
	
	/**
	 * Combines candidates by summing their scores. The candidates are
	 * removed in the process.
	 * 
	 * @param candidates	The candidates
	 * @return				The map from column-concepts to scores
	 */
	private Map<ColumnConcept, Double> sumCandidates(List<ColumnConceptCandidate> candidates) {
		Map<ColumnConcept, Double> candidateMap = new HashMap<ColumnConcept, Double>();

		Iterator<ColumnConceptCandidate> it = candidates.iterator();
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
			it.remove();
		}

		return candidateMap;
	}

	/**
	 * Transfers column-concept candidates from map to list
	 * 
	 * @param candidateMap	The map of candidates and scores
	 * @param candidates	The candidate list
	 */
	private void buildCandidateListFromMap(Map<ColumnConcept, Double> candidateMap, 
			List<ColumnConceptCandidate> candidates) {
		Set<ColumnConcept> columnConcepts = candidateMap.keySet();
		Iterator<ColumnConcept> itColumnConcept = columnConcepts.iterator();
		while (itColumnConcept.hasNext()) {
			ColumnConcept columnConcept = itColumnConcept.next();
			ColumnConceptCandidate candidate 
				= new ColumnConceptCandidate(columnConcept.columnNumber, 
						columnConcept.conceptID, 
						candidateMap.get(columnConcept),
						getId());
			candidates.add(candidate);
		}
	}
	
	/**
	 * Removes candidates that fall below the minimal score.
	 * 
	 * @param candidates	The list of scored column-concept candidates
	 */
	private void applyThreshold(List<ColumnConceptCandidate> candidates) {
		Iterator <ColumnConceptCandidate> it = candidates.iterator();
		while (it.hasNext()) {
			if (it.next().getScore() < threshold) {
				it.remove();
			}
		}	
	}

}
