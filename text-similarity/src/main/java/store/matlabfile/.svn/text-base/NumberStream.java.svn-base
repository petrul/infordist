package store.matlabfile;

/**
 * When you serialize/deserialize a matrix, you may want to treat it as a
 * stream. It should be called TokenStream really, because you can get
 * nextString() from it too.
 * 
 * @author dadi
 */
public interface NumberStream {

	public abstract Integer nextInt();

	public abstract Long nextLong();

	public abstract String nextString();

	public abstract Double nextDouble();

	public abstract int[] nextIntRow();

	public abstract double[] nextDoubleRow();

	public abstract String[] nextStringRow();

	public abstract Integer getRows();

	public abstract Integer getColumns();

	public abstract String getName();

}
