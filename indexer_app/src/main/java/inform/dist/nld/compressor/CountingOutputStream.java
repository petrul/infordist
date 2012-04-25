package inform.dist.nld.compressor;

import java.io.OutputStream;

public class CountingOutputStream extends OutputStream {
	long counter;

	public CountingOutputStream() {
		this.counter = 0;
	}
	
	@Override
	public void write(int b) {
		this.counter ++;
	}

	public long getCounter() {
		return counter;
	}
}
