package eu.trentorise.opendata.columnrecognizers;

import java.util.ListIterator;

/**
 * A FusionColumnRecognizer fuses the output of recognizers preceding it in
 * the recognizer chain. 
 * 
 * It is an abstract parent for fusion recognizers that:
 * - Have a single concept_ID attribute
 * - Produce candidates (<column number>, concept_ID, <score>)
 * - Consume (replace) the fused input candidates
 * 
 * Subclasses need to implement the particular fusion algorithm to be used,
 * for example SVM classifier.
 * 
 * @author Simon
 *
 */
public abstract class FusionColumnRecognizer extends ColumnRecognizer {
	/**
	 * The knowledge base concept ID
	 */
	private long conceptID = -1;

	/**
	 * Constructs the FusionColumnRecognizer.
	 * 
	 * @param id
	 * @param conceptID
	 */
	public FusionColumnRecognizer(String id, long conceptID) {
		super(id);
		this.conceptID = conceptID;
	}

	/**
	 * Gets the knowledge base concept ID.
	 * 
	 * @return	The concept ID
	 */
	public long getConceptID () {
		return conceptID;
	}
	
	/**
	 * Returns true if there is another candidate to process.
	 * 
	 * @param it	Iterator over the candidate list
	 * @return		True if there is another candidate
	 */
	protected boolean hasNextCandidate(ListIterator<ColumnConceptCandidate> it) {
		boolean foundNext = nextCandidate(it) != null;
		if (foundNext) {
			it.previous();
		}
		return foundNext;
	}
	
	/**
	 * Returns the next candidate to process.
	 * 
	 * @param it	Iterator over the candidate list
	 * @return		The next candidate
	 */
	protected ColumnConceptCandidate nextCandidate(ListIterator<ColumnConceptCandidate> it) {
		boolean foundNext = false;
		ColumnConceptCandidate nextCandidate = null;
		while (foundNext && it.hasNext()) {
			nextCandidate = it.next();
			foundNext = (nextCandidate.getConceptID() == conceptID);
		}
		return foundNext ? nextCandidate : null;
	}
	
	/**
	 * Updates the candidate list after processing a candidate.
	 * By default, it removes the last candidate from the list if it should be
	 * consumed.
	 * 
	 * @param it				The iterator to the candidate list
	 * @param lastCandidate		The last element returned by the iterator
	 */
	protected void updateCandidates(ListIterator<ColumnConceptCandidate> it, 
			ColumnConceptCandidate lastCandidate) {
		if (consumeCandidate(lastCandidate)) {
			it.remove();
		}
	}

	/**
	 * Returns true if the candidate should be consumed (replaced by the 
	 * output of the fusion). 
	 * 
	 * @param candidate	The candidate
	 * @return			True if the candidate should be removed
	 */
	protected boolean consumeCandidate(ColumnConceptCandidate candidate) {
		return candidate.getConceptID() == conceptID;
	}

}
