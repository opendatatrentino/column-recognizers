package eu.trentorise.opendata.columnrecognizers.test;

import eu.trentorise.opendata.columnrecognizers.FileUtils;
import eu.trentorise.opendata.columnrecognizers.TFIDFVector;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class TFIDFVectorTest {
	
	/**
	 * Tests the reading and writing of vectors from files.
	 */
	@Test
	public void test() {
		final String VECTOR_PATH = "/models/test_vector_tfidf.txt";
		final String DIFFERENT_VECTOR_PATH = "/models/test_vector_2_tfidf.txt";
		final String IDENTICAL_VECTOR_PATH = "test_vector_tfidf_tmp.txt";
		
		File vectorFile = FileUtils.getResourceFile(VECTOR_PATH);
		File differentVectorFile = FileUtils.getResourceFile(DIFFERENT_VECTOR_PATH);
		File identicalVectorFile = FileUtils.getTmpFile(IDENTICAL_VECTOR_PATH);
		identicalVectorFile.deleteOnExit();
		
		TFIDFVector vector = TFIDFVector.readFromFile(vectorFile);
		vector.writeToFile(identicalVectorFile);
		TFIDFVector identicalVector = TFIDFVector.readFromFile(identicalVectorFile);
		
		assertTrue(vector.cosineSimilarity(identicalVector) == 1.0);
		
		TFIDFVector differentVector = TFIDFVector.readFromFile(differentVectorFile);
		assertTrue(vector.cosineSimilarity(differentVector) < 1.0);
		assertTrue(vector.cosineSimilarity(differentVector) > 0);
	}

}
