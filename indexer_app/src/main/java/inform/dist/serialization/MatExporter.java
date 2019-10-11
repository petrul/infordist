package inform.dist.serialization;

import inform.dist.MatrixAccessor;

public interface MatExporter {

	public abstract void writeScalar(String varName, long value, String comment);

	public abstract void writeMatrix(String varName, MatrixAccessor<? extends Object> matrix, String comment);

	/**
	 * use this if you want to immortalize a String[]
	 */
	public abstract void writeStringArray(String varName, String[] strings, String comment);
	
	public void close();

	public void flush();

}