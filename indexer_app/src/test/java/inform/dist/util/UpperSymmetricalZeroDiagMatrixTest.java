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

	static Logger LOG = Logger.getLogger(UpperSymmetricalZeroDiagMatrixTest.class);
}
