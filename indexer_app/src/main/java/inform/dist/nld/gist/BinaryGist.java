package inform.dist.nld.gist;

import inform.dist.Constants;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import inform.dist.nld.gist.combining.GistCombiningPolicy;
import org.apache.log4j.Logger;
import org.junit.Assert;

/**
 * Unlike a {@link FileGist} or a {@link StringGist} which contains words as strings. We can
 * spare some (lots of) bytes, by coding each word as a Short (16-bit = 2 bytes)
 *
 * update 2019 haven't really tried this idea back in 2010.
 * 
 * @author dadi
 *
 */
public class BinaryGist {
	
	List<List<Short>> codes;

	public BinaryGist(List<List<Short>> codes) {
		this.codes = codes;
	}
	
	/**
	 * Codes a {@link StringListGist} to shorts, respecting a maximum limit.
	 * 
	 * @param maxBytes the approximate memory limit in bytes of this
	 */
	public BinaryGist(StringGist stringListGist, Map<String, Short> codes, int maxBytes) {
		int byteCounter = 0;
		this.codes = new ArrayList<>();
		for (String context : stringListGist.getStringList()) {
			if (byteCounter > maxBytes) {
				if (LOG.isDebugEnabled()) LOG.debug("reached limit of " + maxBytes);
				break; 
			}
			String[] words = context.split(Constants.GIST_WORD_SEPARATOR);
			List<Short> crtCtxt = new ArrayList<Short>(words.length);
			for (String w : words) {
				Short s = codes.get(w);
				if (s == null) {
					continue;
				}
				crtCtxt.add(s);
			}
			crtCtxt.add(codes.get(Constants.GIST_CONTEXT_SEPARATOR));
			this.codes.add(crtCtxt);
			byteCounter += (words.length + 1) * 2;
		}
	}
	
	public BinaryGist(byte[] bytes) {
		List<List<Short>> codes = new ArrayList<>();
		List<Short> crt = new ArrayList<>();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
		try {
			while(true) {
				short nextShort = dis.readShort();
				crt.add(nextShort);
				if (nextShort == Short.MIN_VALUE + 1) {// end of context marker
					codes.add(crt);
					crt = new ArrayList<>();
				}
			}
		} catch (EOFException e) {
			if (crt.size() > 0) codes.add(crt);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		this.codes = codes;
	}

	public void combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy, OutputStream outputStream) {
		this.combine(anotherGist, 1, outputStream);
	}
	
	public void combine(Gist anotherGist, int interweaveBlockSize, OutputStream outputStream) {
		
		BinaryGist theOther = (BinaryGist) anotherGist;
		List<List<Short>> newCodes = new ArrayList<List<Short>>((int) (this.nrLines() + anotherGist.nrLines()));
		
		int counter_this = 0;
		int counter_that = 0;
		
		long size_this = this.nrLines();
		long size_that = anotherGist.nrLines();
		
		while (counter_this < size_this || counter_that < size_that) {
			{
				// this
				List<List<Short>> chunk_of_this = new ArrayList<List<Short>>();
				int chunk_of_this_size = 0;
				while (counter_this < size_this && chunk_of_this_size < interweaveBlockSize) {
					List<Short> row = this.codes.get(counter_this++);
					chunk_of_this.add(row);
					chunk_of_this_size += row.size() * Short.SIZE / 8;
				}
				newCodes.addAll(chunk_of_this);
			}
			
			{
				// that
				List<List<Short>> chunk_of_that = new ArrayList<List<Short>>();
				int chunk_of_that_size = 0;
				while (counter_that < size_that && chunk_of_that_size < interweaveBlockSize) {
					List<Short> row = theOther.codes.get(counter_that++);
					chunk_of_that.add(row);
					chunk_of_that_size += row.size() * Short.SIZE / 8;
				}
				newCodes.addAll(chunk_of_that);
			}
		}
		
		this.codes = newCodes;
		throw new RuntimeException("unimpl");
	}

	public int nrLines() {
		return this.codes.size();
	}

	public void writeTo(OutputStream os) {
		try {
			DataOutputStream daos = new DataOutputStream(os);
			for (List<Short> row : this.codes) {
				for (short s : row) {
					daos.writeShort(s);
				}
			}
			daos.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BinaryGist clone() {
		ArrayList<List<Short>> newCodes = new ArrayList<List<Short>>(this.codes.size());
		for (int i = 0; i < this.codes.size(); i++) {
			List<Short> crtRow = this.codes.get(i);
			ArrayList<Short> copiedRow = new ArrayList<Short>(crtRow.size());
			for (int j = 0; j < crtRow.size(); j++)
				copiedRow.add(crtRow.get(j));
			newCodes.add(copiedRow);
		}
		return new BinaryGist(newCodes);
	}

	/**
	 * 	A map term -> short code. You should only call this once. 
	 */
	public static Map<String, Short> termCodesMapping (String terms[]) {
		Assert.assertTrue(terms.length < (0x01 << Short.SIZE) - 2); // can only code about 2 ^ 16 terms (about 65 000) using short
		
		HashMap<String, Short> result = new HashMap<String, Short>(terms.length + 1);
		
		short code = Short.MIN_VALUE;
		
		result.put(Constants.GIST_THIS_WORD_MARKER, code++);
		
		result.put(Constants.GIST_CONTEXT_SEPARATOR, Constants.GIST_BINARY_CONTEXT_SEPARATOR);
		code++;
		
		for (int i = 0; i < terms.length; i++) {
			result.put(terms[i], code++);
		}
		
		return result;
	}


	@Override
	public String toString() {
		final int maxiter = 10;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < this.codes.size(); i++) {
			List<Short> crt = this.codes.get(i);
			for (Short sh : crt) {
				sb.append(sh).append(Constants.GIST_WORD_SEPARATOR);
			}
			sb.append(Constants.GIST_CONTEXT_SEPARATOR);
			if (i > maxiter) {
				sb.append("[...]\n");
				break;
			}
		}
		return sb.toString();
	}
	
	
	StringGist decode(Map<String, Short> mapping) {
		Map<Short, String> revMapping = new HashMap<Short, String>(mapping.size());
		for (String s : mapping.keySet()) {
			Short code = mapping.get(s);
			revMapping.put(code, s);
		}
		List<String> rows = new ArrayList<String>(this.codes.size());
		
		for (int i = 0; i < this.codes.size(); i++) {
			StringBuilder sb = new StringBuilder();
			List<Short> crtCodeRow = this.codes.get(i);
			for (Short s : crtCodeRow) {
				sb.append(revMapping.get(s));
				sb.append(Constants.GIST_WORD_SEPARATOR);
			}
			sb.deleteCharAt(sb.length() - 1); // delete last blank
			Assert.assertEquals('\n', sb.charAt(sb.length() - 1));
			sb.deleteCharAt(sb.length() - 1); 
			sb.deleteCharAt(sb.length() - 1); // delete CR and the space before
			rows.add(sb.toString());
		}
		
//		return new StringGist(rows);
		throw new RuntimeException("undefined");
	}
	
	public long getSizeInBytes() {
		long size = 0;
		for (List<Short> row : this.codes) {
			size += (row.size() * Short.SIZE / 8);
		}
		return size;
	}

	public InputStream openStreamForReading() {
		throw new RuntimeException("undefined");
	}

	public OutputStream openStreamForWriting() {
		throw new RuntimeException("undefined");
	}

	Logger LOG = Logger.getLogger(BinaryGist.class);

	public List<List<Short>> getCodes() {
		return codes;
	}

//	@Override
	public List<BinaryGist> getSubgists(int subgistSizeInBytes) {
		List<BinaryGist> result = new ArrayList<BinaryGist>();
		int crtSize = 0;
		List<List<Short>> subcodes = new ArrayList<List<Short>>();
		for (int i = 0; i < this.codes.size(); i++) {
			List<Short> row = this.codes.get(i);
			subcodes.add(row);
			crtSize += row.size() * Short.SIZE / 8;
			if (crtSize > subgistSizeInBytes) {
				result.add(new BinaryGist(subcodes));
				crtSize = 0;
				subcodes = new ArrayList<List<Short>>();
			}
		}
		if (subcodes.size() > 0)
			result.add(new BinaryGist(subcodes));

		return result;
	}
	
}
