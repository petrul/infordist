package inform.dist.ncd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import inform.dist.Constants;
import inform.dist.ncd.gist.BinaryGist;
import inform.dist.ncd.gist.dir.AbstractFilesystemGistDirectory;
import inform.dist.ncd.gist.dir.FsBinaryGistDirectory;
import inform.dist.ncd.compressor.Bzip2Compressor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;


/**
 * any valuable part of thi stest should be rewritten for {@link inform.dist.ncd.gist.FileGist} mainly.
 */

@Ignore("GistComplexity is deprecated still tests on computing complexity should be ok. ")
public class GistComplexityTest {

	@Test
	public void testGetCombinedComplexity() throws Exception {

		File f = new File(System.getProperty("java.io.tmpdir") + "/idx/" + RandomStringUtils.randomAlphabetic(10) + ".gistcomplexitytest");
		f.mkdirs();
		Bzip2Compressor bzip2 = new Bzip2Compressor();
		
		AbstractFilesystemGistDirectory cache = new FsBinaryGistDirectory(f, bzip2);
		GistComplexity gc = new GistComplexity(f, bzip2);
		
		for (int i = 0; i < 5; i++) {
			
			String word1 = RandomStringUtils.randomAlphabetic(10);
			String word2 = RandomStringUtils.randomAlphabetic(8);
			
			BinaryGist gist1 = newGist();
//			cache.storeGist(word1, gist1);
			
			BinaryGist gist2 = newGist();
//			cache.storeGist(word2, gist2);
			
			BinaryGist gist1_reconstituted = (BinaryGist) cache.getGist(word1);
			
			assertThat(gist1_reconstituted.getCodes().size(), is(equalTo(gist1.getCodes().size())));
			
			
			assertThat(gist1_reconstituted.getCodes(), is(equalTo(gist1.getCodes())));
			BinaryGist gist2_reconstituted = (BinaryGist) cache.getGist(word2);
			assertThat(gist2_reconstituted.getCodes(), is(equalTo(gist2.getCodes())));
			
			long cw1 = gc.getComplexity(word1);
			long cw2 = gc.getComplexity(word2);

			LOG.info("cw1 : " + cw1);
			LOG.info("cw2 : " + cw2);
			
//			long cc = gc.getCombinedComplexity(gist1, gist2);
			
//			LOG.info("cc : " + cc);
//			LOG.info("nld : " + DistanceCalculator.getNormalizedDistance((int)cw1, (int)cw2, (int)cc));
//			assertTrue(cc > cw1);
//			assertTrue(cc > cw2);
//			assertTrue(cc < cw1 + cw2); // triangle ineq
		}
		FileUtils.deleteDirectory(f);
	}
	
	
	private BinaryGist newGist() {
		int nContexts = 5000;
		List<List<Short>> codes = new ArrayList<List<Short>>();
		
		Random rnd = new Random();
		for (int i = 0; i < nContexts; i++) {
			int freq = 2 + rnd.nextInt(20);
			ArrayList<Short> crtRow = new ArrayList<Short>(freq);
			for (int j = 0; j < freq;j++) {
				short crtCode = (short) rnd.nextInt(60 * 1000);
				if (crtCode == Constants.GIST_BINARY_CONTEXT_SEPARATOR)
					continue;
				crtRow.add(crtCode);
			}
			crtRow.add(Constants.GIST_BINARY_CONTEXT_SEPARATOR); // context separator
			codes.add(crtRow);
		}
		return new BinaryGist(codes);
	}

	static Logger LOG = Logger.getLogger(GistComplexityTest.class);
}
