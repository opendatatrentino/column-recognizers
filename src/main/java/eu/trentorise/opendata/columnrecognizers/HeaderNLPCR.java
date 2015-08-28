package eu.trentorise.opendata.columnrecognizers;

import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLText;

import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * @author Simon
 *
 */
public class HeaderNLPCR extends HeaderBasedCR {


	/**
	 * @param id
	 */
	public HeaderNLPCR(String id, Table table) {
		super(id, table);
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.ColumnRecognizer#computeScoredCandidates(java.util.List)
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		List<NLText> nlTexts = NLPUtils.processTexts(getHeaders());

		int columnNumber = 1;
		Iterator<NLText> itNLText = nlTexts.iterator();
		while (itNLText.hasNext()) {
			NLText nlText = itNLText.next();
			Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
			candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, getId(), meanings));
			columnNumber++;
		}
	}

}
