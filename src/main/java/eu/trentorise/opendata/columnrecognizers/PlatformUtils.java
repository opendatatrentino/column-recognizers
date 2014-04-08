package eu.trentorise.opendata.columnrecognizers;

/**
 * Utilities for working with native binaries.
 * 
 * @author Simon
 *
 */
public class PlatformUtils {
	
	/**
	 * Tries to determine if the OS is Windows or Linux.
	 * 
	 * @return true if Windows
	 */
	public static boolean isOSWindows() {
		return System.getProperty("os.name").startsWith("Windows");
	}
	
	/**
	 * Tries to determine if the running JVM is 32 or 64 bit.
	 * 
	 * @return true if 64 bit
	 */
	public static boolean isJVM64Bit() {
		boolean is64Bit = false;
		String architecture = System.getProperty("os.arch");
		if (architecture.contains("64")) {
			is64Bit = true;
		} else if (architecture.contains("86")) {
			is64Bit = false;
		} else {
			throw new RuntimeException("Could not determine 32 or 64 bit JVM");
		}
		return is64Bit;
	}

	/**
	 * Returns a string describing the platform, such as 'win64' or 'linux32'.
	 * 
	 * @return		The platform name
	 */
	public static String getPlatformName() {
		String os = isOSWindows() ? "win" : "linux";
		String bits = isJVM64Bit() ? "64" : "32";
		return os + bits;
	}
	
	/**
	 * Returns a platform-dependent suffix to be appended to the name of an 
	 * executable file. 
	 * 
	 * @return	The extension
	 */
	public static String getExeExtension() {
		return isOSWindows() ? ".exe" : "";
	}
	
}
