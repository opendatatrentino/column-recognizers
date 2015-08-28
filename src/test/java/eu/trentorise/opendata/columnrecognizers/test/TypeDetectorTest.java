package eu.trentorise.opendata.columnrecognizers.test;

import eu.trentorise.opendata.columnrecognizers.TypeDetector;
import static org.junit.Assert.*;

import org.junit.Test;

import eu.trentorise.opendata.nlprise.DataTypeGuess;
import eu.trentorise.opendata.nlprise.DataTypeGuess.Datatype;
import eu.trentorise.opendata.nlprise.typecheckers.EmptyTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.FloatTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.IntTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.JsonTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.ListTypeChecker;
import eu.trentorise.opendata.nlprise.typecheckers.XmlTypeChecker;

/**
 * @author Simon
 *
 */
public class TypeDetectorTest {
	private static final String intString = "12";
	private static final String floatString = "3.14";
	private static final String stringString = "hello world";

	@Test
	public void test() {
		// Currently we don't use the date type due to an issue with
		// the Parboiled library.
		assertTrue(IntTypeChecker.check(intString));
		assertTrue(FloatTypeChecker.check(floatString));
		assertFalse(XmlTypeChecker.check(stringString));
		assertFalse(JsonTypeChecker.check(stringString));
		assertFalse(EmptyTypeChecker.check(stringString));
		assertFalse(ListTypeChecker.check(stringString));
		
		assertTrue(TypeDetector.guessType(intString) == Datatype.INT);
		assertTrue(TypeDetector.guessType(floatString) == Datatype.FLOAT);
		assertTrue(TypeDetector.guessType(stringString) == Datatype.STRING);
	}

}
