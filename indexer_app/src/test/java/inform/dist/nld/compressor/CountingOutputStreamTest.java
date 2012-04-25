package inform.dist.nld.compressor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

public class CountingOutputStreamTest {

	@Test
	public void testWriteInt() throws IOException  {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CountingOutputStream cos = new CountingOutputStream();
		
		Random rnd = new Random();
		for (int i = 0; i < 1000; i++) {
			int nr = rnd.nextInt();
			baos.write(nr);
			cos.write(nr);
		}
		
		assertEquals(cos.getCounter(), baos.toByteArray().length);
	}

}
