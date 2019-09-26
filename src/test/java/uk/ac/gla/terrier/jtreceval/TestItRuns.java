package uk.ac.gla.terrier.jtreceval;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestItRuns 
{
	@Test public void testBasicTrecEval()
	{
		assertEquals(0, new trec_eval().run(new String[]{"-h"}));
	}
	
	@Test public void testBasicGdeval()
	{
		new gdeval().runAndGetOutput(new String[] {"-help"});
	}
	
	@Test(expected=RuntimeException.class) public void testBasicGdevalFails()
	{
		new gdeval().runAndGetOutput(new String[] {"NOT_AN_ARGUMENT"});
	}
	
	@Test public void testBasicNdeval()
	{
		new ndeval().runAndGetOutput(new String[] {"-help"});
	}
	
	@Test(expected=RuntimeException.class) public void testBasicNdevalFails()
	{
		new ndeval().runAndGetOutput(new String[] {"NOT_AN_ARGUMENT"});
	}
}