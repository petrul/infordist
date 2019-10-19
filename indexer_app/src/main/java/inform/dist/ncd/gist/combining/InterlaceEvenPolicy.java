package inform.dist.ncd.gist.combining;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.log4j.Logger;

import inform.dist.ncd.gist.Gist;

/**
 * this is the description of combining in INTERLACE_EVEN mode for StringListGists
 */
public class InterlaceEvenPolicy extends GistCombiningPolicy {
    @Override
    public void combine(Gist g1, Gist g2, OutputStream outputStream) {

        final PrintWriter writer = new PrintWriter(new BufferedOutputStream(outputStream));
        
        int switcher = 0;

        final long size1 = g1.getSizeInBytes();
        final long size2 = g2.getSizeInBytes();

        double dbl_ratio1 = ((double) size1) / ((double) size2);
        double dbl_ratio2 = ((double) size2) / ((double) size1);

        // for every ratio1 lines of g1 there will follow ratio2 lines of g2
        int ratio1 = Math.max(1, (int) Math.round(dbl_ratio1));
        int ratio2 = Math.max(1, (int) Math.round(dbl_ratio2));

        final int[] ratios = new int[] { ratio1, ratio2 };

        Iterator<String> it1 = g1.iterator();
        Iterator<String> it2 = g2.iterator();

        final Iterator<String>[] iterators = new Iterator[] { it1, it2};

        while (it1.hasNext() || it2.hasNext()) {
            final Iterator<String> it = iterators[switcher];
            final int ratio = ratios[switcher];
            for (int i = 0; i < ratio && it.hasNext(); i++)
                writer.println(it.next());

            switcher = 1 - switcher;
        }

        writer.flush();
        writer.close();

    }

    static Logger LOG = Logger.getLogger(InterlaceEvenPolicy.class);
}