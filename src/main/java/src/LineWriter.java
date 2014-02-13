import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * The LineWriter is an abstract class for writing out lines
 * to a file.
 * 
 * @author Simon
 *
 */
public abstract class LineWriter {
	/**
	 * The output file
	 */
	private File file = null;

	/**
	 * Constructs the LineWriter
	 * 
	 * @param file	The output file
	 */
	public LineWriter(File file) {
		super();
		this.file = file;
	}
	
	/**
	 * Writes the lines to the file.
	 */
	public void write() {
	    Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
			        new FileOutputStream(file), "utf-8"));
			while (hasNext()) {
				writer.write(String.format("%s%n", next()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Returns true if there is more lines to write.
	 * 
	 * @return	True if there is more to write
	 */
	protected abstract boolean hasNext();

	/**
	 * Returns the next line to write.
	 * 
	 * @return	The next line
	 */
	protected abstract String next();
}
