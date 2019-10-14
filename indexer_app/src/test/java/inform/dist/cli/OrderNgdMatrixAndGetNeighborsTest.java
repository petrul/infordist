package inform.dist.cli;

import inform.dist.cli.OrderNgdMatrixAndGetNeighbors.Select;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

public class OrderNgdMatrixAndGetNeighborsTest {

	@Test
	public void testRun() throws Exception {

		String s = "#created from wikipedia dump [/infres/ir300/ic2/dimulesc/work/infordist/not_versioned/enwiki-latest-pages-articles.xml],term list [/infres/ir300/ic2/dimulesc/work/infordist/not_versioned/terms-sup10.txt]\n"
				+ "# name:terms\n"
				+ "# type: string-array\n"
				+ "# size:3\n"
				+ "\n0\t\"hello\""
				+ "\n1\t\"mean\""
				+ "\n2\t\"world\""
				+ "\n# name:distances\n"
				+ "# type: matrix\n"
				+ "# rows:3\n"
				+ "# columns:3"
				+ "\n0   2 5"
				+ "\n3   0 1" + "\n2.1 1 0";
		StringReader in = new StringReader(s);

		StringWriter out = new StringWriter();
		OrderNgdMatrixAndGetNeighbors sorter = new OrderNgdMatrixAndGetNeighbors(
				in,
				"terms",
				"distances",
				3,
				true,
				Select.SMALLEST,
				out);

		sorter.run();

		String buffer = out.getBuffer().toString();
		String[] lines = buffer.split("\n");
		int trueLines = 0;
		for (String line : lines) {
			if ("".equals(line))
				continue;
			trueLines++;
			String[] elems = line.split(",");
			Assert.assertEquals(3, elems.length); // 3 with the latest comma
		}
		Assert.assertEquals(3, trueLines);

	}

	Logger LOG = Logger.getLogger(OrderNgdMatrixAndGetNeighborsTest.class);
}
