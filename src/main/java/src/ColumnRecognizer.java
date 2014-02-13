import java.util.List;


/**
 * ColumnRecognizer is the abstract superclass for column recognizers.
 * 
 * @author Simon
 *
 */
public abstract class ColumnRecognizer {
	public abstract List<ColumnConceptCandidate> computeScoredCandidates();
	
}
