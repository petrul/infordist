package infordist.gg;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class CombinedComplexityJobManagerTest {

	@Test
	public void testSplitArrayInChunks() {
		List<String> aList = Arrays.asList("Marry", "Joe", "Jack");
		List<List<String>> chunks = CombinedComplexityJobManager.splitArrayInChunks(aList, 2);
		assertEquals(2, chunks.size());
		
		aList = Arrays.asList("Marry", "Joe");
		chunks = CombinedComplexityJobManager.splitArrayInChunks(aList, 2);
		assertEquals(1, chunks.size());
		
		aList = Arrays.asList("Marry", "Joe", "Jack");
		chunks = CombinedComplexityJobManager.splitArrayInChunks(aList, 24);
		assertEquals(1, chunks.size());
		
		List<String> emptyList = Collections.emptyList();
		chunks = CombinedComplexityJobManager.splitArrayInChunks(emptyList, 2);
		assertEquals(0, chunks.size());
	}

}
