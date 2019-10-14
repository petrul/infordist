package inform.dist.nld.compressor;

import static org.junit.Assert.assertTrue;

import inform.dist.nld.gist.StringGist;
import inform.dist.nld.gist.StringListGist;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class PPMCompressorTest {

	@Test
	public void testCompress() {
		PPMCompressor compressor = new PPMCompressor();
		StringGist slg = newStringListGist();
		assertTrue(slg.nrLines() > 10);
		assertTrue(slg.getSizeInBytes() > 10 * 1000);
		
		LOG.info(slg.getSizeInBytes());
		
		byte[] compressed = compressor.compress(slg);
		assertTrue(compressed.length < slg.getSizeInBytes());
		LOG.info("compressed : " + compressed.length);
		
//		byte[] uncompressed = compressor.uncompress(new ByteArrayInputStream(compressed));
//		LOG.info("uncompressed : " + uncompressed.length);
//		String _s = new String(uncompressed, Constants.UTF8_CHARSET);
//		List<String> list_s = Arrays.asList(_s.split("\n"));
//		StringListGist recreated_gist = new StringListGist(list_s);
//		Assert.assertEquals(slg, recreated_gist);
	}

	private StringGist newStringListGist() {
		List<String> gist = new ArrayList<String>();
		for (int i = 0; i < 1000; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < 8; j++) {
				sb.append(RandomStringUtils.randomAlphabetic(10));
				sb.append(" ");
			}
			gist.add(sb.toString());
		}
		return new StringGist(String.join("\n",gist));
	}

	static Logger LOG = Logger.getLogger(PPMCompressorTest.class);
}
