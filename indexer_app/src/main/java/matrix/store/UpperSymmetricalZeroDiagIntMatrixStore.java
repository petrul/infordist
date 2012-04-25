package matrix.store;

import inform.dist.MatrixAccessor;

public class UpperSymmetricalZeroDiagIntMatrixStore implements IntMatrixStore {

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public int[] copyOfRow(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub

	}

	@Override
	public int get(int i, int j) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getColumns() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRows() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init(int i) {
		// TODO Auto-generated method stub

	}

	@Override
	public void put(int i, int j, int elem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putRow(int i, int[] elems) {
		// TODO Auto-generated method stub

	}

	@Override
	public void set(MatrixAccessor<Integer> mtx) {
		throw new RuntimeException("unimpl");
	}

	@Override
	public void reopen() {
		throw new RuntimeException("unimpl");		
	}
}
