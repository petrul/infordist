package inform.dist.serialization;

import inform.dist.util.NumberStream;

public interface MatReader {

	/**
	 * gets a stream of data corresponding the the variable named by the
	 * parameter.
	 */
	public abstract NumberStream getNumberStream(String name);

	/**
	 * 
	 * @return a String[] that was previously stored into this .mat file
	 * using 
	 */
	public abstract String[] getStringArray(String varName);
	
	public Long getLongScalar(String varName);

}