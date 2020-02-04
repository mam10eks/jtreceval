package uk.ac.gla.terrier.jtreceval;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import org.junit.Test;
import org.junit.Assert;

public class GDEvalIntegrationTest {
	private static final File TEST_QREL = resource("small-example.qrels");
	private static final File TEST_RUN = resource("small-example.run");
	
	@Test
	public void approveNdcgAt20ForTestResources() {
		EvalReport expected = new EvalReport();
		expected.amean = 0.71837;
		expected.scorePerTopic = Arrays.asList(0.43675, 1.0);
		
		EvalReport actual = new gdeval().ndcg_20_report(TEST_QREL, TEST_RUN);
		
		assertEquals(expected, actual);
	}

	@Test
	public void approveNdcgForTestResources() {
		EvalReport expected = new EvalReport();
		expected.amean = 0.7184;
		expected.scorePerTopic = Arrays.asList(0.4367, 1.0);
		
		EvalReport actual = new trec_eval().reportMeasure("ndcg", TEST_QREL, TEST_RUN);
		
		assertEquals(expected, actual);
	}
	
	@Test
	public void approveMapForTestResources() {
		EvalReport expected = new EvalReport();
		expected.amean = 0.6389;
		expected.scorePerTopic = Arrays.asList(0.2778, 1.0);
		
		EvalReport actual = new trec_eval().reportMeasure("map", TEST_QREL, TEST_RUN);
		
		assertEquals(expected, actual);
	}
	
	private static void assertEquals(EvalReport a, EvalReport b) {
		Assert.assertEquals(a.amean, b.amean, 0e-3);
		Assert.assertEquals(a.scorePerTopic.size(), b.scorePerTopic.size());
		
		for(int i=0; i<a.scorePerTopic.size(); i++) {
			Assert.assertEquals(a.scorePerTopic.get(i), b.scorePerTopic.get(i), 0e-3);
		}
	}
	
	private static File resource(String name) {
		return Paths.get("src").resolve("test").resolve("resources").resolve(name).toFile();
	}
}
