package inform.dist.nld.gist;


public interface GistDirectory {
	
	/**
	 * this should be faster than simply testing for null getGist
	 */
	boolean hasGist(String term);
	
	Gist getGist(String term);
	
	void storeGist(String term, Gist gist);
	
//	void storeGist(String term, List<String> string);
}
