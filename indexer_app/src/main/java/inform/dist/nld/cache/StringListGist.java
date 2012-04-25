package inform.dist.nld.cache;

import inform.dist.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This gist uses a List of Strings in order to represent the succession of contexts. Each context
 * is a String.
 * @author dadi
 *
 */
public class StringListGist implements Gist {
	
	List<String> gist;
	
	public StringListGist(List<String> list) {
		this.gist = new ArrayList<String>();
		this.gist.addAll(list);
	}
	
	public StringListGist(String string) {
		String[] strings = string.split(Constants.GIST_CONTEXT_SEPARATOR);
		List<String> list = Arrays.asList(strings);
		this.gist = list;
	}

	@Override
	public void combine(Gist anotherGist) {
		StringListGist gist2 = (StringListGist) anotherGist;
		List<String> combination = new ArrayList<String>(this.gist.size() + gist2.gist.size());
//		combination.addAll(this.gist);
//		combination.addAll(gist2.gist);
	
		long len_g1 = this.size();
		long len_g2 = gist2.size();
		
		int cnt_g1 = 0;
		int cnt_g2 = 0;
		
		while (cnt_g1 < len_g1 || cnt_g2 < len_g2) {
			if (cnt_g1 < len_g1) {
				String str = this.gist.get(cnt_g1);
				combination.add(str);
				cnt_g1++;
			}
			if (cnt_g2 < len_g2) {
				String str = gist2.gist.get(cnt_g2);
				combination.add(str);
				cnt_g2++;
			}
		}
		
		this.gist = combination;
	}

	@Override
	public long size() {
		return this.gist.size();
	}


	
	public void writeToSorted(OutputStream os) {
		int len = gist.size();
		for (int i = 0; i < len; i++) {
			try {
				String s = gist.get(i);
				String[] words = s.split(" ");
				Arrays.sort(words);
				int words_length = words.length;
				for (int j = 0; j < words_length; j++) {
					String w = words[j];
					os.write(w.getBytes(Constants.UTF8_ENCODING));
					if (j != words_length - 1)
						os.write(' ');
				}
				if (i != len - 1)
					os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public void writeTo(OutputStream os) {
		int len = gist.size();
		for (int i = 0; i < len; i++) {
			try {
				String s = gist.get(i);
				os.write(s.getBytes(Constants.UTF8_ENCODING));
				
				if (i != len - 1)
					os.write(Constants.GIST_CONTEXT_SEPARATOR.getBytes());
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public Gist clone() {
		return new StringListGist(this.gist);
	}
	
	public List<String> getStringList() {
		return gist;
	}

	@Override
	public List<Gist> getSubgists(int maxSizeInBytes) {
		List<Gist> subgists = new ArrayList<Gist>();
		List<String> crtSubgist = new ArrayList<String>();
		int crtCounter = 0;
		
		for (String s : this.gist) {
			if (crtCounter > maxSizeInBytes) { 
				subgists.add(new StringListGist(crtSubgist));
				crtSubgist = new ArrayList<String>();
				crtCounter = 0;
			}
			crtSubgist.add(s);
			crtCounter += s.length() + 1;
		}
		
		if (crtSubgist.size() > 0)
			subgists.add(new StringListGist(crtSubgist));

		return subgists;
	}

	@Override
	public long getSizeInBytes() {
		long result = 0;
		for (String s: this.gist) {
			result += (s.length() + 1);
		}
		return result;
	}
	
//	public long sizeInBytes() {
//		long size = 0;
//		for (String s : this.gist) {
//			size += s.length() + 1;
//		}
//		return size;
//	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		int counter = 0;
		for (String s : this.gist) {
			sb.append(s).append("\n");
			if (counter++ > 5) {
				sb.append("[...]");
				break;
			}
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		StringListGist slg = (StringListGist) obj;
		return this.gist.equals(slg.gist);
	}
	
}
