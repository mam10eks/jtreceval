package uk.ac.gla.terrier.jtreceval;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class ndeval {
	private static File ndeval = null;
	
	public ndeval() {
		ndeval = getNdEvalBinary();
	}
	
	static synchronized File getNdEvalBinary()
	{
		if (ndeval != null)
			return ndeval;
		final String resName = getExecName();
		if (trec_eval.class.getClassLoader().getResource(resName) == null)
			throw new UnsupportedOperationException("Unsupported os/arch: " + resName);
		
		File tempExec = null;
		try{
			Path tempExecDir = Files.createTempDirectory("jtrec_eval");
			if (trec_eval.DELETE)
				tempExecDir.toFile().deleteOnExit();
		
			tempExec = File.createTempFile("ndeval", ".exe", tempExecDir.toFile());
			InputStream in = trec_eval.class.getClassLoader().getResourceAsStream(resName);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tempExec));			
			IOUtils.copy(in, out);
			in.close();
			out.close();
			tempExec.setExecutable(true);
			if (trec_eval.DELETE)
				tempExec.deleteOnExit();
			
			// FIXME handle dependences as well as in trec_eval.java
		} catch (Exception e) {
			throw new UnsupportedOperationException(e);
		}
		assert tempExec.exists() : "Exe file " + tempExec.toString() + " does not exist after creation";
		return tempExec;
	}
	
	private ProcessBuilder getBuilder(String[] args)
	{
		List<String> cmd = new ArrayList<String>();
		cmd.add(ndeval.getAbsolutePath().toString());
		for(String arg : args)
			cmd.add(arg);
		return new ProcessBuilder(cmd);
	}
	
	/** Obtain the output from a trec_eval invocation
	 * 
	 * @param args trec_eval commandline arguments
	 * @return first dimension is for each line, second dimension is for each component
	 */
	public String[][] runAndGetOutput(String[] args)
	{
		List<String[]> output = new ArrayList<String[]>();
		try{
			ProcessBuilder pb = getBuilder(args);
			pb.redirectError(Redirect.INHERIT);
			Process p = pb.start();
			InputStream in = p.getInputStream();
			LineIterator it = IOUtils.lineIterator(new InputStreamReader(in));
			while(it.hasNext())
			{
				output.add(it.next().split(","));
			}
			p.waitFor();
			int exit = p.exitValue();
			if (exit != 0)
				throw new RuntimeException("ndeval ended with non-zero exit code ("+exit+"): " + output);
			
			return output.toArray(new String[output.size()][]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public Map<String, Double> evaluate(File qrels, File runFile) {
		String[][] output = runAndGetOutput(new String[] {qrels.toString(), runFile.toString()});
		String[] resultRow = output[output.length -1];
		
		if(!resultRow[1].equals("amean")) {
			throw new RuntimeException("Format changed?" + output[1]);
		}
		
		Map<String, Integer> fieldIndizes = new HashMap<String, Integer>();
		
		for(int i=2; i< output[0].length; i++) {
			fieldIndizes.put(output[0][i], i);
		}
		
		Map<String, Double> ret = new HashMap<String, Double>();
		
		for(Entry<String, Integer> measure : fieldIndizes.entrySet()) {
			ret.put(measure.getKey(), Double.parseDouble(resultRow[measure.getValue()]));
		}
		
		return ret;
	}

	private static String getExecName() {
		return 	"ndeval-" + trec_eval.getOSShort() + "-" + System.getProperty("os.arch");
	}
	
	public static void main(String[] args) {
		ndeval ndeval = new ndeval();
		System.out.println(ndeval.evaluate(new File("/tmp/tmp-qrel3976145828610178693/qrels"), new File("/tmp/tmp-unzipped3173560149087468956/tmp-unzipped")));
	}
}
