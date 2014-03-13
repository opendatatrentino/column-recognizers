package eu.trentorise.opendata.columnrecognizers;

//import it.unitn.disi.sweb.core.nlp.INLPPipeline;
import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLText;
//import it.unitn.disi.sweb.core.nlp.parameters.NLPParameters;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;

/**
 * @author Simon
 *
 */
public class HeaderBasedCR extends ColumnRecognizer {
//    @Autowired
//    @Qualifier("ODHPipeline")
//    private INLPPipeline<NLPParameters> headerPipeline;

    /**
	 * The column headers
	 */
	List<String> headers = null;

	/**
	 * @param id
	 */
	public HeaderBasedCR(String id, Table table) {
		super(id);
		headers = table.getHeaders();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.ColumnRecognizer#computeScoredCandidates(java.util.List)
	 */
	@Override
	public void computeScoredCandidates(List<ColumnConceptCandidate> candidates) {
		List<NLText> nlTexts = NLPUtils.processTexts(headers);

		int columnNumber = 1;
		Iterator<NLText> itNLText = nlTexts.iterator();
		while (itNLText.hasNext()) {
			NLText nlText = itNLText.next();
			Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
			candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, getId(), meanings));
			columnNumber++;
		}
		
//		int columnNumber = 1;
//		Iterator<String> itHeader = headers.iterator();
//		while (itHeader.hasNext()) {
//			String header = itHeader.next();
//			NLText nlText = headerPipeline.runPipeline(header);
//			Set<NLMeaning> meanings = NLPUtils.extractMeanings(nlText);
//			candidates.addAll(NLPUtils.meaningsToCandidates(columnNumber, "", meanings));
//			columnNumber++;
//		}
	}

}
