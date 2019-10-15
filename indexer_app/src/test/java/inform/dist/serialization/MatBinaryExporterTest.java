package inform.dist.serialization;

import inform.dist.MatrixAccessor;
import inform.dist.util.NumberStream;

import java.io.File;
import java.nio.BufferUnderflowException;
import java.util.Random;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class MatBinaryExporterTest {

	@Test
	public void testWriteMatrix() throws Exception {
		File tempFile = File.createTempFile(this.getClass().getCanonicalName(), "ser");
		MatBinaryExporter exporter = new MatBinaryExporter(tempFile, "hi");

		try {
			long scalar = 456778;
			exporter.writeScalar("scalar", scalar, "asd");
			
			int nrstrings = 456;
			String[] terms = new String[nrstrings];
			
			for (int i = 0; i < nrstrings; i++) {
				terms[i] = RandomStringUtils.randomAlphabetic(200);
			}
			exporter.writeStringArray("strarr", terms, "hello");
			
			final int rows = 500;
			final int columns = 1200;

			Random rnd = new Random();
			final double[][] arr = new double[rows][columns];
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < columns; j++)
					arr[i][j] = rnd.nextDouble();
			}
			
			MatrixAccessor<? extends Object> mtx = new MatrixAccessor<Double>() {

				@Override
				public Double getCell(int i, int j) {
					return arr[i][j];
				}

				@Override
				public int getColumns() {
					return columns;
				}

				@Override
				public int getRows() {
					return rows;
				}

			};
			exporter.writeMatrix("mymat", mtx, "comment");
			exporter.close();
			
			/*
			 * now read, see if it was properly written
			 */
			
			MatBinaryReader reader = new MatBinaryReader(tempFile);
			
			Long nextlong = reader.getLongScalar("scalar");
			Assert.assertEquals((long)scalar, (long)nextlong);
			
			
			String[] reread_terms = reader.getStringArray("strarr");
			for (int i = 0; i < reread_terms.length; i++)
				Assert.assertEquals(terms[i], reread_terms[i]);
			
			NumberStream numberStream;
			numberStream = reader.getNumberStream("mymat");
			Assert.assertEquals((int)rows, (int)numberStream.getRows());
			Assert.assertEquals((int)columns, (int)numberStream.getColumns());
			

			//			double[][] reread_arr = new double[rows][];
			for (int i = 0; i < rows; i++) {
				double[] reread_arr; 
				try { 
					reread_arr= numberStream.nextDoubleRow();
				} catch (BufferUnderflowException e) {
					LOG.info("i = " + i);
					throw e;
				}
				Assert.assertEquals(columns, reread_arr.length);
				for (int j = 0; j < columns; j++) {
					Assert.assertEquals(
							"not equal for i = " + i + " and j = " + j + "", 
							reread_arr[j], arr[i][j], 0.0);
				}
			}
			
		} finally {
			FileUtils.deleteQuietly(tempFile);
		}
	}

	Logger LOG = Logger.getLogger(MatBinaryExporterTest.class);
}
