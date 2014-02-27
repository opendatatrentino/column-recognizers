/**
 * 
 */
package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * SVMLightWriter writes out an example file (for training or classification)
 * for SVM-Light.
 * 
 * @author Simon
 *
 */
public class SVMLightWriter extends LineWriter {
	/**
	 * Iterator over the feature vectors
	 */
	Iterator<List<Double>> itFeatures = null;

	/**
	 * @param file		The output file
	 * @param features	The feature vector for each example
	 */
	public SVMLightWriter(File file,
			List<List<Double>> features) {
		super(file);
		this.itFeatures = features.iterator();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.LineWriter#hasNext()
	 */
	@Override
	protected boolean hasNext() {
		return itFeatures.hasNext();
	}

	/* (non-Javadoc)
	 * @see eu.trentorise.opendata.columnrecognizers.LineWriter#next()
	 */
	@Override
	protected String next() {
		StringBuilder sb = new StringBuilder("0");
		Iterator<Double> it = itFeatures.next().iterator();
		int featureNumber = 1;
		while (it.hasNext()) {
			sb.append(String.format(Locale.US, " %d:%f", featureNumber++, it.next()));
		}
		return sb.toString();
	}
}
