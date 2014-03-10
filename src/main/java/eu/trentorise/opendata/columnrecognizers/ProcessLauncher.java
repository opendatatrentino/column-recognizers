package eu.trentorise.opendata.columnrecognizers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The StreamGobbler class alleviates a problem associated  
 * with launching processes under Windows: it is necessary 
 * to drain the output and error streams of the process,
 * otherwise it will hang when the buffers fill up.
 * 
 * The code is based on the article:
 * 
 * When Runtime.exec() won't
 * By Michael C. Daconta, JavaWorld.com, 12/29/00
 * http://www.javaworld.com/jw-12-2000/jw-1229-traps.html
 * 
 * 
 * @author Simon
 *
 */
class StreamGobbler extends Thread
{
	InputStream is;
	String type;

	StreamGobbler(InputStream is, String type)
	{
		this.is = is;
		this.type = type;
	}

	public void run()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			while ( (line = br.readLine()) != null)
				System.out.println(type + ">" + line);    
		} catch (IOException ioe)
		{
			ioe.printStackTrace();  
		}
	}
}

/**
 * ProcessLauncher launches an executable and waits for it to terminate.
 * 
 * @author Simon
 *
 */
public class ProcessLauncher {
	public static int run(String[] commandArray) {
		return run(commandArray, null, new File("."));
	}
	
	public static int run(String[] commandArray,
			String[] environmentVariables,
			File directory) {
		Process process;
		int exitValue = 0;

		try {
			long time0 = System.currentTimeMillis();
			
			process = Runtime.getRuntime().exec(
					commandArray, 
					environmentVariables,
					directory);
            StreamGobbler errorGobbler = new 
                StreamGobbler(process.getErrorStream(), "ERROR");            
            StreamGobbler outputGobbler = new 
                StreamGobbler(process.getInputStream(), "OUTPUT");
                
            errorGobbler.start();
            outputGobbler.start();
			exitValue = process.waitFor();
			long time1 = System.currentTimeMillis();
			long timeDiff = time1 - time0;			
			System.out.println("Elapsed: " + timeDiff + "milliseconds");
		} catch (IOException e) {
			throw new RuntimeException("Could not launch process", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Could not launch process", e);
		}
		return exitValue;
		
	}
}
