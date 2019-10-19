package inform.dist.ncd.gist.dir;


import inform.dist.ncd.gist.Gist;

@Deprecated
public interface GistDirectory {
	
	/**
	 * this should be faster than simply testing for null getGist
	 */
	boolean hasGist(String term);
	
	Gist getGist(String term);
	
	void storeGist(String term, Gist gist);
	
//	void storeGist(String term, List<String> string);
}
