package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

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
	 * The name of the executable that does training of the SVM classifier
	 */
	private final static String SVM_LEARNER_NAME = "svm_learn";
	
	/**
	 * The name of the executable that does SVM classification
	 */
	private final static String SVM_CLASSIFIER_NAME = "svm_learn";
	
	/**
	 * Name of the default column recognizer specification file
	 */
	private final static String DEFAULT_SPECIFICATION_FILE_NAME = "column-recognizers.txt";
	
	/**
	 * Path for finding model files among the application resources
	 */
	private final static String MODEL_RESOURCE_PATH = "/models/";
	
	/**
	 * The top-level column recognizers folder
	 */
	private static File columnRecognizersRoot = null;
	
	/**
	 * The data folder
	 */
	private static File dataFolder = null;
	
	/**
	 * The folder with the SVM-Light executables
	 */
	private static File svmExecutablesFolder = null;

	/**
	 * Sets the folder with the SVM-Light executables. Use this if you want
	 * to put the folder in a location different from the default.
	 * 
	 * @param svmExecutablesFolder The folder with the SVM-Light executables
	 */
	public static void setSVMExecutablesFolder(File svmExecutablesFolder) {
		FileUtils.svmExecutablesFolder = svmExecutablesFolder;
	}

	/**
	 * Sets the folder containing column recognizer data, configuration, and
	 * model files. Use this if you want to put the folder in a location 
	 * different from the default. 
	 * 
	 * @param dataFolder	the data folder
	 */
	public static void setDataFolder(File dataFolder) {
		FileUtils.dataFolder = dataFolder;
	}

	/**
	 * Gets the folder that holds ML models and other data
	 * 
	 * @return	The folder
	 */
	public static File getDataFolder() {
		if (dataFolder == null) {
			dataFolder = new File(getRoot(), DATA_FOLDER_NAME);
		}
		return dataFolder;
	}
	
	/**
	 * Gets the folder with the SVM-Light executables
	 */
	public static File getSVMExecutablesFolder() {
		if (svmExecutablesFolder == null) {
//			URL url = FileUtils.class.getResource("/" + SVM_EXECUTABLES_FOLDER_NAME);
//			svmExecutablesFolder = new File(url.getPath());
			svmExecutablesFolder = getResourceFile("/" + SVM_EXECUTABLES_FOLDER_NAME);
		}
		return svmExecutablesFolder;
	}

	/**
	 * Gets the executable that does training of the SVM classifier
	 * 
	 * @return	The executable file
	 */
	public static File getSVMLearner() {
		return new File(getSVMExecutablesFolder(), SVM_LEARNER_NAME);
	}

	/**
	 * Gets the executable that does SVM classification
	 * 
	 * @return	The executable file
	 */
	public static File getSVMClassifier() {
		return new File(getSVMExecutablesFolder(), SVM_CLASSIFIER_NAME);
	}
	
	/**
	 * Gets the column recognizer specification file used in training the given
	 * fusion recognizer.
	 * 
	 * @param recognizerID		The name of the classifier fusion recognizer
	 * @return					The CR specification file
	 */
	public static File getSVMTrainingCRSpecificationFile(String recognizerID) {
		return new File(getSVMModelFolder(recognizerID), "recognizers-" + recognizerID + ".txt");
	}

	/**
	 * Gets the file containing an SVM classifier model for a fusion 
	 * recognizer. 
	 * 
	 * @param recognizerID		The name of the recognizer
	 * @return					The model file
	 */
	public static File getSVMModelFile(String recognizerID) {
		return new File(getSVMModelFolder(recognizerID), "svm-model-" + recognizerID);
	}
	
	/**
	 * Returns the directory with the model and training files for an SVM 
	 * classifier column recognizer.
	 * <p>
	 * Looks first amoung application resources, then in the data directory.
	 * If the folder is missing from the data directory, it is created.
	 * 
	 * @param recognizerID		The name of the recognizer
	 * @return					The directory
	 */
	private static File getSVMModelFolder(String recognizerID) {
		File svmModelFolder = null;
		String svmModelFolderName = "svm-" + recognizerID;
//		URL folderURL = FileUtils.class.getResource(MODEL_RESOURCE_PATH + svmModelFolderName);
		URL folderURL = getResourceURL(MODEL_RESOURCE_PATH + svmModelFolderName);
		boolean foundInResources = folderURL != null;
		if (foundInResources) {
			svmModelFolder = new File(folderURL.getPath());
		} else {
			svmModelFolder = new File(getDataFolder(), svmModelFolderName);
			sureDirectory(svmModelFolder);
		}

		return svmModelFolder;
	}

	/**
	 * Returns the file containing labeled training examples for a given SVM
	 * classifier column recognizer.
	 * 
	 * @param recognizerID
	 * @return
	 */
	public static File getSVMTrainingFile(String recognizerID) {
		return new File(getSVMModelFolder(recognizerID), "svm-examples-" + recognizerID);
	}

	/**
	 * Ensures the existence of the specified directory.
	 * 
	 * @param directory		The directory
	 */
	private static void sureDirectory(File directory) {
		if (directory.exists()) {
			if (!directory.isDirectory()) {
				throw new RuntimeException(
						"Cannot create directory (file with the same name already exists): "
						+ directory.getPath());
			}
		} else {
			boolean success = directory.mkdir();
			if (!success) {
				throw new RuntimeException("Failed to create directory: " + directory.getPath());
			}
		}
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

	/**
	 * Gets the default column recognizer specification file.
	 * 
	 * @return	The specification file
	 */
	public static File getDefaultSpecificationFile() {
		return getResourceFile("/" + DEFAULT_SPECIFICATION_FILE_NAME);
//		URL url = FileUtils.class.getResource("/" + DEFAULT_SPECIFICATION_FILE_NAME);
//		return new File(url.getPath());
	}

	/**
	 * Returns a model file from the working directory or a resource.
	 *  
	 * @param modelPath		The relative path to the model file
	 * @return				The file
	 */
	public static File getModelFile(String modelPath) {
		File modelFile = null;
//		URL url = FileUtils.class.getResource(MODEL_RESOURCE_PATH + modelPath);
		URL url = getResourceURL(MODEL_RESOURCE_PATH + modelPath);
		if (url != null) {
			modelFile = new File(url.getPath());
		} else {
			modelFile = new File(modelPath);
		}
		return modelFile;
	}

	/**
	 * Returns a model file from the working directory, a resource, or any of 
	 * the model directories supplied by the caller.
	 * 
	 * @param modelPath				The relative path to the model file
	 * @param modelDirectories		A list of model directories
	 * @return						The file 
	 */
	public static File getModelFile(String modelPath, List<File> modelDirectories) {
		File modelFile = getModelFile(modelPath);
		if (modelFile == null || !modelFile.exists()) {
			Iterator<File> it = modelDirectories.iterator();
			boolean foundModel = false;
			File probe = null;
			while (!foundModel && it.hasNext()) {
				probe = new File(it.next(), modelPath);
				foundModel = probe.exists();
			}
			if (foundModel) {
				modelFile = probe;
			}
		}
		return modelFile;
	}

	/**
	 * Gets a file from the application resources.
	 * 
	 * @param path	The resource path (including leading '/')
	 * @return		The file
	 */
	public static File getResourceFile(String path) {
		URL url = FileUtils.class.getResource(path);
		return new File(url.getPath());
	}
	
	/**
	 * Checks if a resource file exists.
	 * 
	 * @param path		The resource path (including leading '/')
	 * @return			True if the file exists
	 */
	public static boolean resourceExists(String path) {
		return FileUtils.class.getResource(path) != null;
	}

	/**
	 * Gets a URL for a resource file.
	 * 
	 * @param path	The resource path (including leading '/')
	 * @return		The file URL
	 */
	public static URL getResourceURL(String path) {
		return FileUtils.class.getResource(path);
	}
	
	/**
	 * Returns the current temp file directory. If it doesn't exist, it is 
	 * created.
	 * 
	 * @return The directory
	 */
	public static File getTmpDirectory() {
		File tmpDirectory = new File(System.getProperty("java.io.tmpdir"));
		sureDirectory(tmpDirectory);
		return tmpDirectory;
	}
	
	/**
	 * Returns a file in the tmp file directory.
	 * 
	 * @param tmpFileName	The name of the file
	 * @return				A file in the tmp directory
	 */
	public static File getTmpFile(String tmpFileName) {
		return new File(getTmpDirectory(), tmpFileName);
	}
}
