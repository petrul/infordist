package inform.dist.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * test for {@link UpperSymmetricalZeroDiagMatrix}
 * @author dadi
 *
 */
public class UpperSymmetricalZeroDiagMatrixTest {

	@Test
	public void testGet() {

		int n = 103;
		int[][] arr = new int[n][n];
		UpperSymmetricalZeroDiagMatrix economicalMatrix = new UpperSymmetricalZeroDiagMatrix(n);
		
		for (int i = 0; i < n; i++) {
			arr[i][i] = 0;
		}
		
		
		// fill the upper part
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int newVal = new Random().nextInt();
				arr[i][j] = newVal;
				arr[j][i] = newVal;
				try {
				economicalMatrix.set(i, j, newVal);
				} catch (RuntimeException e) {
					LOG.error("runtimex for " + i + ", " + j);
					throw e;
				}
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				assertEquals(arr[i][j], economicalMatrix.get(i, j));
				assertEquals(arr[i][j], economicalMatrix.get(j,i));
				assertEquals(arr[j][i], economicalMatrix.get(i, j));
			}
		}

		
		// fill the lower part
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				int newVal = new Random().nextInt();
				arr[i][j] = newVal;
				arr[j][i] = newVal;
				
				// the change is here, inversion of i,j => j,i
				economicalMatrix.set(j, i, newVal);
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				assertEquals(arr[i][j], economicalMatrix.get(i, j));
				assertEquals(arr[i][j], economicalMatrix.get(j,i));
				assertEquals(arr[j][i], economicalMatrix.get(i, j));
			}
		}
		
	}

	@Test
	public void testComputeArraySizeDoesNotOverflowForLargeN() {
		// n=55000: n*(n-1) = 3,024,945,000, which overflows a 32-bit int
		// (wraps to a negative value) before ever reaching the /2 --
		// reproduces the exact NegativeArraySizeException(-635011148) seen
		// with a real 55000-term run. Tested via the pure size-computation
		// method rather than actually constructing the matrix, since the
		// correct result here is ~1.5 billion ints (~6GB) -- too large to
		// safely allocate in a unit test.
		long expected = 55000L * 54999L / 2L;
		assertEquals(expected, UpperSymmetricalZeroDiagMatrix.computeArraySize(55000));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComputeArraySizeRejectsNTooLargeForAnyJavaArray() {
		// n large enough that even n*(n-1)/2 itself exceeds Integer.MAX_VALUE
		// -- no int[] can ever hold this many elements, so this must fail
		// loudly instead of silently truncating/wrapping.
		UpperSymmetricalZeroDiagMatrix.computeArraySize(100_000);
	}

	static Logger LOG = Logger.getLogger(UpperSymmetricalZeroDiagMatrixTest.class);
}
