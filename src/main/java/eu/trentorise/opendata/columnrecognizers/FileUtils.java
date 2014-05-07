package eu.trentorise.opendata.columnrecognizers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
	private final static String SVM_CLASSIFIER_NAME = "svm_classify";
	
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
	 * Looks first among application resources, then in the data directory.
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
	 * @return	The specification file input stream
	 */
	public static InputStream getDefaultSpecificationFile() {
		return getResourceStream("/" + DEFAULT_SPECIFICATION_FILE_NAME);
		
//		return getResourceFile("/" + DEFAULT_SPECIFICATION_FILE_NAME);
//		URL url = FileUtils.class.getResource("/" + DEFAULT_SPECIFICATION_FILE_NAME);
//		return new File(url.getPath());
	}

	/**
	 * Returns a model file from the working directory or a resource.
	 *  
	 * @param modelPath		The relative path to the model file
	 * @return				The file
	 */
//	public static File getModelFile(String modelPath) {
	public static InputStream getModelFile(String modelPath) {
//		File modelFile = null;
		InputStream modelStream = getResourceStream(MODEL_RESOURCE_PATH + modelPath);
		if (modelStream == null) {
			File modelFile = new File(modelPath);
			if (modelFile.exists()) {
				try {
					modelStream = new FileInputStream(modelFile);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		URL url = getResourceURL(MODEL_RESOURCE_PATH + modelPath);
//		if (url != null) {
//			modelFile = new File(url.getPath());
//		} else {
//			modelFile = new File(modelPath);
//		}
//		return modelFile;
		return modelStream;
	}

	/**
	 * Returns a model file from the working directory, a resource, or any of 
	 * the model directories supplied by the caller.
	 * 
	 * @param modelPath				The relative path to the model file
	 * @param modelDirectories		A list of model directories
	 * @return						The file 
	 */
//	public static File getModelFile(String modelPath, List<File> modelDirectories) {
	public static InputStream getModelFile(String modelPath, List<File> modelDirectories) {
//		File modelFile = getModelFile(modelPath);
		InputStream modelStream = getModelFile(modelPath);
//		if (modelFile == null || !modelFile.exists()) {
		if (modelStream == null) {
			Iterator<File> it = modelDirectories.iterator();
			boolean foundModel = false;
			File probe = null;
			while (!foundModel && it.hasNext()) {
				probe = new File(it.next(), modelPath);
				foundModel = probe.exists();
			}
			if (foundModel) {
//				modelFile = probe;
				try {
					modelStream = new FileInputStream(probe);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		return modelFile;
		return modelStream;
	}

	/**
	 * Gets a file from the application resources.
	 * 
	 * @param path	The resource path (including leading '/')
	 * @return		The file
	 */
	public static File getResourceFile(String path) {
		URL url = FileUtils.class.getResource(path);
		File file = null;
		try {
			URI uri = url.toURI();
			file = new File(uri);
			if (!file.exists()) {
				file = getTmpFile(file.getName());
				file.deleteOnExit();
				extractFromJar(path, file);
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		return new File(url.getPath());
		return file;
	}
	
	/**
	 * Gets an input stream from resources. Call this rather than 
	 * getResourceFile if the resource may be in a jar.
	 * 
	 * @param path	The resource path (including leading '/')
	 * @return		The input stream
	 */
	public static InputStream getResourceStream(String path) {
		return FileUtils.class.getResourceAsStream(path);
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

	/**
	 * Extracts a resource (such as an executable file) from inside the jar and
	 * writes it to a destination outside the jar.
	 * <p>
	 * The code is derived from the post "run exe which is packaged inside jar"
	 * at StackOverflow.
	 * <p> 
	 * http://stackoverflow.com/questions/600146/run-exe-which-is-packaged-inside-jar
	 * 
	 * @param resource		The URL to the resource
	 * @param destination	The destination file
	 */
	public static void extractFromJar(String resourcePath, File destination) {
		try {
            final ZipEntry entry;
            final InputStream zipStream;
            OutputStream fileStream;
            final String entryName;
            
            final URI jarURI = getJarURI();
//            entryName = jarURI.relativize(resource.toURI()).getPath();
            entryName = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

			@SuppressWarnings("resource")
			ZipFile zipFile = new ZipFile(new File(jarURI));
			entry = zipFile.getEntry(entryName);

            if(entry == null)
            {
                throw new FileNotFoundException("cannot find file: " + entryName + " in archive: " + zipFile.getName());
            }

            zipStream  = zipFile.getInputStream(entry);
            fileStream = null;

            try
            {
                final byte[] buf;
                int          i;

                fileStream = new FileOutputStream(destination);
                buf        = new byte[1024];
                i          = 0;

                while((i = zipStream.read(buf)) != -1)
                {
                    fileStream.write(buf, 0, i);
                }
            }
            finally
            {
            	if (zipStream != null) {
            		zipStream.close();
            	}
            	if (fileStream != null) {
            		fileStream.close();
            	}
            }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();			
		}
	}
	
	/**
	 * Returns the URI of the current jar.
	 * 
	 * The code is from the post "run exe which is packaged inside jar"
	 * at StackOverflow.
	 * <p> 
	 * http://stackoverflow.com/questions/600146/run-exe-which-is-packaged-inside-jar
	 */
    private static URI getJarURI()
            throws URISyntaxException
        {
            final ProtectionDomain domain;
            final CodeSource       source;
            final URL              url;
            final URI              uri;

            domain = FileUtils.class.getProtectionDomain();
            source = domain.getCodeSource();
            url    = source.getLocation();
            uri    = url.toURI();

            return (uri);
        }
    
//    private static URI getFile(final URI    where,
//    		final String fileName)
//    				throws ZipException,
//    				IOException
//    {
//    	final File location;
//    	final URI  fileURI;
//
//    	location = new File(where);
//
//    	// not in a JAR, just return the path on disk
//    	if(location.isDirectory())
//    	{
//    		fileURI = URI.create(where.toString() + fileName);
//    	}
//    	else
//    	{
//    		final ZipFile zipFile;
//
//    		zipFile = new ZipFile(location);
//
//    		try
//    		{
//    			fileURI = extract(zipFile, fileName);
//    		}
//    		finally
//    		{
//    			zipFile.close();
//    		}
//    	}
//
//    	return (fileURI);
//   }


//    private static URI extract(final ZipFile zipFile,
//                                   final String  fileName)
//            throws IOException
//        {
//            final File         tempFile;
//            final ZipEntry     entry;
//            final InputStream  zipStream;
//            OutputStream       fileStream;
//
//            tempFile = File.createTempFile(fileName, Long.toString(System.currentTimeMillis()));
//            tempFile.deleteOnExit();
//            entry    = zipFile.getEntry(fileName);
//
//            if(entry == null)
//            {
//                throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());
//            }
//
//            zipStream  = zipFile.getInputStream(entry);
//            fileStream = null;
//
//            try
//            {
//                final byte[] buf;
//                int          i;
//
//                fileStream = new FileOutputStream(tempFile);
//                buf        = new byte[1024];
//                i          = 0;
//
//                while((i = zipStream.read(buf)) != -1)
//                {
//                    fileStream.write(buf, 0, i);
//                }
//            }
//            finally
//            {
//                close(zipStream);
//                close(fileStream);
//            }
//
//            return (tempFile.toURI());
//        }

//        private static void close(final Closeable stream)
//        {
//            if(stream != null)
//            {
//                try
//                {
//                    stream.close();
//                }
//                catch(final IOException ex)
//                {
//                    ex.printStackTrace();
//                }
//            }
//        }
}
