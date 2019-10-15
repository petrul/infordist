package inform.dist.ncd.gist;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import inform.dist.Constants;
import inform.dist.ncd.compressor.Bzip2Compressor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("will ignore binary gists for now")
public class BinaryGistTest {

	@Test
	public void testBuildTermMapping() {
		String[] terms = { "a", "b", "c" };
		Map<String, Short> codes = BinaryGist.termCodesMapping(terms);
		Short code = Short.MIN_VALUE;

		assertNotNull(codes);
		assertEquals(terms.length + 2, codes.size());

		assertThat(code++, equalTo(codes.get("_")));
		assertThat(code++, equalTo(codes.get("\n")));
		assertThat(code++, equalTo(codes.get("a")));
		assertThat(code++, equalTo(codes.get("b")));
		assertThat(code++, equalTo(codes.get("c")));
	}

//	@Test
//	public void testCoding() {
//		Map<String, Short> codes = new HashMap<String, Short>();
//		codes.put("_", (short) 0);
//		codes.put("\n", (short) 1);
//		codes.put("a", (short) 2);
//		codes.put("b", (short) 3);
//		codes.put("c", (short) 4);
//
//		StringGist slg = new StringGist("a b c\nc b a _ b\na");
//
//		{
//			// code the entire string
//			BinaryGist icg = new BinaryGist(slg, codes, 100);
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			icg.writeTo(baos);
//			byte[] bytes = baos.toByteArray();
//			byte[] expected = new byte[] { 0, 2, 0, 3, 0, 4, 0, 1, 0, 4, 0, 3, 0, 2, 0, 0, 0, 3, 0, 1, 0, 2, 0, 1 };
//			assertThat(bytes, equalTo(expected));
//		}
//
//		{
//			// code only the first line
//			BinaryGist icg = new BinaryGist(slg, codes, 6);
//			// LOG.info(icg);
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			icg.writeTo(baos);
//			byte[] bytes = baos.toByteArray();
//			byte[] expected = new byte[] { 0, 2, 0, 3, 0, 4, 0, 1 };
//			assertThat(bytes, equalTo(expected));
//		}
//
//		{
//			// code only the first two lines
//			BinaryGist icg = new BinaryGist(slg, codes, 8);
//			// LOG.info(icg);
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			icg.writeTo(baos);
//			byte[] bytes = baos.toByteArray();
//			byte[] expected = new byte[] { 0, 2, 0, 3, 0, 4, 0, 1, 0, 4, 0, 3, 0, 2, 0, 0, 0, 3, 0, 1 };
//			assertThat(bytes, equalTo(expected));
//		}
//
//	}

	@Test
	public void testConstruction() {

		Bzip2Compressor bzip2 = new Bzip2Compressor();
		byte[] bytes = bzip2.uncompress(this.getClass().getClassLoader().getResourceAsStream("hors.bz2"));
		String text = new String(bytes, Constants.UTF8_CHARSET);
		String[] split = text.split("\\s+");

		String[] terms;
		{
			Set<String> set = new HashSet<String>();
			for (String s : split) {
				if (s.equals("_"))
					continue;
				set.add(s);
			}
			terms = (String[]) set.toArray(new String[set.size()]);
		}
		LOG.info("nr terms:" + terms.length);
		StringGist slgist = new StringGist(text);
		LOG.info("nr of bytes in the stringlist string :" + slgist.getSizeInBytes());

		Map<String, Short> codes = BinaryGist.termCodesMapping(terms);
		BinaryGist codeGist = new BinaryGist(slgist, codes, Integer.MAX_VALUE);

		LOG.info("nr of bytes in the codeGist string :" + codeGist.getSizeInBytes());

		Assert.assertTrue(codeGist.nrLines() > 0);
		Assert.assertEquals(codeGist.nrLines(), slgist.nrLines());
		Assert.assertTrue(codeGist.getSizeInBytes() < slgist.getSizeInBytes() / 2);

		codeGist = new BinaryGist(slgist, codes, 10 * 450 * 1000);
		LOG.info("nr of bytes in the codeGist limited to 10 * 450KB :" + codeGist.getSizeInBytes() + " and contexts : "
				+ codeGist.nrLines());
		LOG.info(codeGist);
	}

	@Test
	public void testCombine() {

		short[] shorts1 = { 1, 2, 3, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				4, 5, 6, 7, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				8, 9, Constants.GIST_BINARY_CONTEXT_SEPARATOR };

		short[] shorts2 = { -1, -2, -3, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-4, -5, -6, -7, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-8, -9, -10, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-18, -19, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-18, -19, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-18, -19, Constants.GIST_BINARY_CONTEXT_SEPARATOR, 
				-18, -19, Constants.GIST_BINARY_CONTEXT_SEPARATOR, };

		BinaryGist bg1 = new BinaryGist(this.shortsToBytes(shorts1));
		BinaryGist bg2 = new BinaryGist(this.shortsToBytes(shorts2));

		LOG.info(bg1);
		LOG.info(bg2);

		BinaryGist both = bg1.clone();
//		both.combine(bg2, 2 * 2 * 3); // two lines per block
		assertEquals(10, both.getCodes().size());
		Assert.assertTrue(both.getCodes().get(1).get(0) > 0); // second line should be positive
		LOG.info(both);
		
		
//		both = bg1.clone();
//		both.combine(bg2); // line per line interweaving
		assertEquals(10, both.getCodes().size());
		Assert.assertTrue(both.getCodes().get(1).get(0) < 0); // second line should be negative
		LOG.info(both);
	}

	byte[] shortsToBytes(short[] shorts) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			for (int i = 0; i < shorts.length; i++) {
				dos.writeShort(shorts[i]);
			}
			dos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testCombine2() {
		BinaryGist positiveGist = this.newGist(1);
		BinaryGist negativeGist = this.newGist(-1);
		
		LOG.info(positiveGist.getSizeInBytes());
		LOG.info(negativeGist.getSizeInBytes());
		
		BinaryGist both = positiveGist.clone();
//		both.combine(negativeGist);
		LOG.info(both.getSizeInBytes());
		Assert.assertEquals(both.getCodes().get(0), positiveGist.getCodes().get(0));
		Assert.assertEquals(both.getCodes().get(1), negativeGist.getCodes().get(0));
		Assert.assertEquals(both.getCodes().get(2), positiveGist.getCodes().get(1));
		Assert.assertEquals(both.getCodes().get(3), negativeGist.getCodes().get(1));
	}

	private BinaryGist newGist(int coeff) {
		int nContexts = 125000; // ca fera un string de 2MB (125000 * 2 * 8)
		List<List<Short>> codes = new ArrayList<List<Short>>();
		
		Random rnd = new Random();
		for (int i = 0; i < nContexts; i++) {
			int freq = 7;
			ArrayList<Short> crtRow = new ArrayList<Short>(freq);
			for (int j = 0; j < freq;j++) {
				short crtCode = (short) (coeff * rnd.nextInt(60 * 1000));
				if (crtCode == Constants.GIST_BINARY_CONTEXT_SEPARATOR)
					continue;
				crtRow.add(crtCode);
			}
			crtRow.add(Constants.GIST_BINARY_CONTEXT_SEPARATOR); // context separator
			codes.add(crtRow);
		}
		return new BinaryGist(codes);
	}

	@Test
	public void testSubgists() {
		BinaryGist gist = this.newGist(1);
//		List<Gist> subgists = gist.getSubgists(450 * 1000);
//		Assert.assertTrue(subgists.size() > 1);
//
//		int sumSizesSubgists = 0;
//		List<List<Short>> codes_again = new ArrayList<List<Short>>();
//		for (Gist g : subgists) {
//			sumSizesSubgists += g.nrLines();
//			codes_again.addAll (((BinaryGist)g).codes);
//		}
		
//		Assert.assertEquals(sumSizesSubgists, gist.nrLines());
//		Assert.assertEquals(codes_again, gist.codes);
	}
	
	/**
	 * codes a string string to binary string and decodes back to string and assert equality
	 */
	@Test
	public void codeAndUncode() {
		int nrTerms = 10000;
		String[] terms = new String[nrTerms];
		
		for (int i = 0; i < terms.length; i++) {
			String crtTerm = RandomStringUtils.randomAlphabetic(10);
			terms[i] = crtTerm;
		}
		Map<String, Short> mapping = BinaryGist.termCodesMapping(terms);
		
		Random rnd = new Random();
		List<String> rows = new ArrayList<String>();
		for (int i = 0; i < 200; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < 7; j++) {
				String randomTerm = terms[rnd.nextInt(terms.length)];
				sb.append(randomTerm).append(" ");
			}
			sb.deleteCharAt(sb.length() - 1);
			//sb.append(randomTerm).append("\n");
			rows.add(sb.toString());
		}
		StringGist slg = new StringGist(rows);
		
		BinaryGist bg = new BinaryGist(slg, mapping, Integer.MAX_VALUE);
		StringGist slg2 = bg.decode(mapping);
		
		assertEquals(slg.getStringList().size(), slg2.getStringList().size());
		for (int i = 0; i < slg.string.length(); i++) {
			String row1 = slg.getStringList().get(i);
			String row2 = slg2.getStringList().get(i);
			assertEquals(row1, row2);		
		}
	
	}
	
	Logger LOG = Logger.getLogger(BinaryGistTest.class);
}
