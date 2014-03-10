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
	private Iterator<List<Double>> itFeatures = null;
	
	/**
	 * Iterator over the labels
	 */
	private Iterator<Double> itLabels = null;

	/**
	 * Constructs the writer with unlabeled feature vectors.
	 * 
	 * @param file				The output file
	 * @param featureVectors	The feature vector for each example
	 */
	public SVMLightWriter(File file,
			List<List<Double>> featureVectors) {
		super(file);
		this.itFeatures = featureVectors.iterator();
	}

	/**
	 * Constructs the writer with feature vectors and labels.
	 * 
	 * @param file				The output file
	 * @param featureVectors	The feature vector for each example
	 * @param labels			The label for each example
	 */
	public SVMLightWriter(File file, 
			List<List<Double>> featureVectors,
			List<Double> labels) {
		super(file);
		assert(labels != null);
		this.itFeatures = featureVectors.iterator();
		this.itLabels = labels.iterator();
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
		final String DEFAULT_LABEL = "0";
		
		StringBuilder sb = new StringBuilder();
		if (itLabels == null) {
			sb.append(DEFAULT_LABEL);
		} else {
			sb.append(itLabels.next());
		}
		Iterator<Double> it = itFeatures.next().iterator();
		int featureNumber = 1;
		while (it.hasNext()) {
			double score = it.next();
			if (score > 0) {
				sb.append(String.format(Locale.US, " %d:%f", featureNumber, score));
			}
			featureNumber++;
		}
		return sb.toString();
	}
}
