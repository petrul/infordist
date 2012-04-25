package inform.dist.nld.compressor;

import static org.junit.Assert.assertEquals;
import inform.dist.Constants;
import inform.dist.nld.cache.StringListGist;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

public class Bzip2CompressorTest {

	Bzip2Compressor bzip2 = new Bzip2Compressor();
	
	@Test
	public void testCompress() throws UnsupportedEncodingException {
		String s = RandomStringUtils.randomAlphabetic(100 * 1000);
		byte[] compressed = bzip2.compress(new StringListGist(s));
		byte[] decompress = bzip2.uncompress(new ByteArrayInputStream(compressed));
		assertEquals(new String(decompress, Constants.UTF8_ENCODING), s);
	}

}
