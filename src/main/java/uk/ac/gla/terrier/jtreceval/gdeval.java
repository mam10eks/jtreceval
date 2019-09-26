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
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class gdeval {

	private static int ERR = 3;
	
	private static File gdeval = null;
	
	public gdeval() {
		gdeval = getGdEvalBinary();
	}
	
	static synchronized File getGdEvalBinary() {
		if (gdeval != null)
			return gdeval;
		final String resName = "gdeval.pl";
		
		File tempExec = null;
		try {
			Path tempExecDir = Files.createTempDirectory("jtrec_eval");
			if (trec_eval.DELETE)
				tempExecDir.toFile().deleteOnExit();
		
			tempExec = File.createTempFile("gdeval", ".pl", tempExecDir.toFile());
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
	
	private	ProcessBuilder getBuilder(String[] args)
	{
		List<String> cmd = new ArrayList<String>();
		cmd.add(gdeval.getAbsolutePath().toString());
		for(String arg : args)
			cmd.add(arg);
		return new ProcessBuilder(cmd);
	}
	
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
				throw new RuntimeException("gdeval ended with non-zero exit code ("+exit+")");
			
			return output.toArray(new String[output.size()][]);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public double err_20(File qrels, File runFile) {
		String[][] output = runAndGetOutput(new String[] {qrels.toPath().toString(), runFile.toPath().toString()});
		
		if(!output[0][ERR].equals("err@20") || !output[output.length-1][1].equals("amean")) {
			throw new RuntimeException("Format changed?" + output[0][ERR]);
		}

		return Double.parseDouble(output[output.length-1][ERR]);
	}
}
