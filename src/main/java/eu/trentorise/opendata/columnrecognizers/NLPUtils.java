package eu.trentorise.opendata.columnrecognizers;

import it.unitn.disi.sweb.core.nlp.model.NLMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLMultiWord;
import it.unitn.disi.sweb.core.nlp.model.NLNamedEntity;
import it.unitn.disi.sweb.core.nlp.model.NLSenseMeaning;
import it.unitn.disi.sweb.core.nlp.model.NLSentence;
import it.unitn.disi.sweb.core.nlp.model.NLText;
import it.unitn.disi.sweb.core.nlp.model.NLToken;
import it.unitn.disi.sweb.webapi.client.IProtocolClient;
import it.unitn.disi.sweb.webapi.client.ProtocolFactory;
import it.unitn.disi.sweb.webapi.client.nlp.PipelineClient;
import it.unitn.disi.sweb.webapi.model.NLPInput;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NLPUtils encapsulates the NLP functionality in the column recognizer library.
 * 
 * @author Simon
 *
 */
public class NLPUtils {

        private static final Logger LOG = LoggerFactory.getLogger(NLPUtils.class);
    
	/**
	 * Extracts all the NLMeanings from the first sentence of an NLText.
	 * 
	 * @param nlText	The NLText
	 * @return			The set of NLMeanings
	 */
	static Set<NLMeaning> extractMeanings(NLText nlText) {
		Set<NLMeaning> meanings = new HashSet();
		
		List<NLSentence> sentences = nlText.getSentences();
		NLSentence firstSentence = sentences.iterator().next();
		List<NLToken> tokens = firstSentence.getTokens();
		List<NLMultiWord> multiWords = firstSentence.getMultiWords();
		List<NLNamedEntity> namedEntities = firstSentence.getNamedEntities();
		
		// Add meanings of all tokens that are not part of multiwords or NEs
		Iterator<NLToken> itToken = tokens.iterator();
		while (itToken.hasNext()) {
			Set<NLMeaning> tokenMeanings = getTokenMeanings(itToken.next());
			if (tokenMeanings != null) {
				meanings.addAll(tokenMeanings);			
			}
//			NLToken token = itToken.next();
//			boolean hasMultiWords = token.getMultiWords() != null && !token.getMultiWords().isEmpty();
//			boolean hasNamedEntities = token.getNamedEntities() != null && !token.getNamedEntities().isEmpty();
//			if (!hasMultiWords && !hasNamedEntities) {
//				if (token.getMeanings() == null || token.getMeanings().isEmpty()) {
//					// This is a hack to handle a bug where the set of meanings
//					// is empty but there is a selected meaning.
//					
//					NLMeaning selectedMeaning = token.getSelectedMeaning();
//					if (selectedMeaning != null) {
//						meanings.add(selectedMeaning);
//					}
//				} else {
//					meanings.addAll(token.getMeanings());
//				}
//			}
		}
		
		// Add meanings of multiwords and NEs
		Iterator<NLMultiWord> itMultiWord = multiWords.iterator();
		while (itMultiWord.hasNext()) {
			NLMultiWord multiWord = itMultiWord.next();
			meanings.addAll(multiWord.getMeanings());
		}
		Iterator<NLNamedEntity> itNamedEntity = namedEntities.iterator();
		while (itNamedEntity.hasNext()) {
			NLNamedEntity namedEntity = itNamedEntity.next();
			meanings.addAll(namedEntity.getMeanings());
		}
		
		return meanings;
	}

	/**
	 * Get the meanings of a token that is not part of a multiword or named entity.
	 * 
	 * @param token	The token
	 * @return		The meanings
	 */
	private static Set<NLMeaning> getTokenMeanings(NLToken token) {
		Set<NLMeaning> meanings = null;
		boolean hasMultiWords = token.getMultiWords() != null && !token.getMultiWords().isEmpty();
		boolean hasNamedEntities = token.getNamedEntities() != null && !token.getNamedEntities().isEmpty();
		if (!hasMultiWords && !hasNamedEntities) {
			if (token.getMeanings() == null || token.getMeanings().isEmpty()) {
				// This is a hack to handle a bug where the set of meanings
				// is empty but there is a selected meaning.
				
				NLMeaning selectedMeaning = token.getSelectedMeaning();
				if (selectedMeaning != null) {
					meanings = new HashSet();
					meanings.add(selectedMeaning);
				}
			} else {
				meanings = token.getMeanings();
			}
		}
		return meanings;
	}

	/**
	 * Turns a list of meanings to a list of candidates
	 * 
	 * @param columnNumber	The column number for the candiates
	 * @param originator	The ID of the orginating recognizer
	 * @param meanings		The meanings
	 * @return				The candidates
	 */
	public static List<ColumnConceptCandidate> meaningsToCandidates(
			int columnNumber, String originator, Set<NLMeaning> meanings) {
		List<ColumnConceptCandidate> candidates = new ArrayList();
		Iterator<NLMeaning> itMeanings = meanings.iterator();
		while (itMeanings.hasNext()) {
			NLMeaning meaning = itMeanings.next();
			if (meaning instanceof NLSenseMeaning) {
				ColumnConceptCandidate candidate = new ColumnConceptCandidate(
						columnNumber,
						((NLSenseMeaning)meaning).getGlobalId(),
						meaning.getProbability(), 
						originator);
				candidates.add(candidate);
			}
		}
		return candidates;
	}
	
	/**
	 * Finds the NLMeaning with the highest probability.
	 */
	public static NLMeaning findMaxProbabilityMeaning(Set<NLMeaning> meanings) {
		NLMeaning maxMeaning = null;
		Iterator<NLMeaning> itMeanings = meanings.iterator();
		while (itMeanings.hasNext()) {
			NLMeaning meaning = itMeanings.next();
			if (maxMeaning == null) {
				maxMeaning = meaning;
			} else {
				maxMeaning = pickMaxProbabilityMeaning(maxMeaning, meaning);
			}
		}
			
		return maxMeaning;
	}
	
	/**
	 * Chooses the meaning with the highest probability and, when probabilities
	 * are equal, breaks the tie by prefering the meaning with the lowest
	 * globalID.
	 * 
	 * @param nlMeaning1	A meaning
	 * @param nlMeaning2	Another meaning
	 * @return				The meaning with higher probability
	 */
	private static NLMeaning pickMaxProbabilityMeaning(NLMeaning nlMeaning1, NLMeaning nlMeaning2) {
		Float probability1 = nlMeaning1.getProbability();
		Float probability2 = nlMeaning2.getProbability();
		
		NLMeaning maxMeaning = nlMeaning1;
		if (probability1 > probability2) {
			maxMeaning = nlMeaning1;
		} else if (probability1 < probability2) {
			maxMeaning = nlMeaning2;
		} else if (nlMeaning1 instanceof NLSenseMeaning && nlMeaning2 instanceof NLSenseMeaning) {
			if (((NLSenseMeaning)nlMeaning1).getGlobalId() < ((NLSenseMeaning)nlMeaning2).getGlobalId()) {
				maxMeaning = nlMeaning1;
			} else {
				maxMeaning = nlMeaning2;
			}
		}
		
		return maxMeaning;
	}

	/**
	 * Runs the NLP pipeline on a list of texts.
	 * 
	 * @param texts The texts
	 * @return      The NLTexts
	 */
	public static List<NLText> processTexts(List<String> texts) {
//        IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH, "ui.disi.unitn.it", 8092);
            LOG.warn("TODO - USING HARDCODED ENGLISH when creating sweb client in processTexts");
        IProtocolClient api = ProtocolFactory.getHttpClient(Locale.ENGLISH);
		PipelineClient pipelineClient = new PipelineClient(api);
        NLPInput input = new NLPInput();
        input.setText(texts);
        //input.setNlpParameters(params);

//      NLText[] result = pipelineClient.run("KeywordTextPipeline", input, 1l);
        NLText[] result = pipelineClient.run("ODHPipeline", input, 1l);
         
		return Arrays.asList(result);
	}

}
