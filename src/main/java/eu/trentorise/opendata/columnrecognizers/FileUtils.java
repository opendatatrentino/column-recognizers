package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.net.URL;

/**
 * The all-static FileUtils class centralizes the task of locating files
 * that the system depends on.
 * 
 * @author Simon
 *
 */
public class FileUtils {
	/**
	 * The name of the top-level column recognizers folder
	 */
	private final static String COLUMN_RECOGNIZERS_ROOT_NAME = "column-recognizers";
	
	/**
	 * Name of the folder that holds ML models and other data
	 */
	private final static String DATA_FOLDER_NAME = "sample-data";
	
	/**
	 * The folder with the SVM-Light executables
	 */
	private final static String SVM_EXECUTABLES_FOLDER_NAME = "svm-light";
	
	/**
	 * The top-level column recognizers folder
	 */
	private static File columnRecognizersRoot = null;
	
	/**
	 * Gets the folder that holds ML models and other data
	 * 
	 * @return	The folder
	 */
	public static File getDataFolder() {
		return new File(getRoot(), DATA_FOLDER_NAME);
	}
	
	/**
	 * Gets the folder with the SVM-Light executables
	 */
	public static File getSVMExecutablesFolder() {
		return new File(getRoot(), SVM_EXECUTABLES_FOLDER_NAME);
	}
	
	/**
	 * Gets the top-level column recognizers folder
	 * 
	 * @return		The top folder
	 */
	private static File getRoot() {
		if (columnRecognizersRoot == null) {
			File directory = getClassDirectory();
			boolean foundCRRoot = false;
			boolean reachedFileSystemRoot = false;
			while (!foundCRRoot && !reachedFileSystemRoot) {
				if (isCRRoot(directory)) {
					foundCRRoot = true;
					columnRecognizersRoot = directory;
				} else {
					File parent = directory.getParentFile();
					if (parent != null) {
						directory = parent;
					} else {
						reachedFileSystemRoot = true;
					}
				}
			}
		}
		
		return columnRecognizersRoot;
	}

	/**
	 * Checks if the directory is the top-level column recognizers folder.
	 * 
	 * @param directory		A directory
	 * @return				True if the directory is the column recognizers root
	 */
	private static boolean isCRRoot(File directory) {
		return directory.isDirectory() && directory.getName().equals(COLUMN_RECOGNIZERS_ROOT_NAME);
	}

	/**
	 * Gets the directory that stores this class.
	 * 
	 * @return	The class directory
	 */
	private static File getClassDirectory() {
		URL url = new FileUtils().getClass().getProtectionDomain().getCodeSource().getLocation();
		return new File(url.getPath());
	}
	
}