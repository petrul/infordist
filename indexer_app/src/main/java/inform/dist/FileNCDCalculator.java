package inform.dist;

import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.gist.FileGist;

import java.io.File;
import java.net.URL;

/**
 * usable to compute NCD for two files
 */
public class FileNCDCalculator {

    private final Compressor compressor;
    FileGist g1;
    FileGist g2;

    long uncd = -1;
    double ncd = -1.0;

    long c1;
    long c2;
    long cc;


    public FileNCDCalculator(URL url1, URL url2, Compressor compressor) {
        this(new FileGist(url1), new FileGist(url2), compressor);
    }

    public FileNCDCalculator(File f1, File f2, Compressor compressor) {
        this(new FileGist(f1), new FileGist(f2), compressor);

    }

    public FileNCDCalculator(FileGist g1, FileGist g2, Compressor compressor) {
        this.g1 = g1;
        this.g2 = g2;
        this.compressor = compressor;
    }


    public void compute() {
        this.c1 = g1.computeComplexity(compressor);
        this.c2 = g2.computeComplexity(compressor);
        this.cc = g1.combine(g2).computeComplexity(compressor);
    }

    public long getUnnormalizedDistance() {
        if (uncd >= 0)
            return uncd;

        this.uncd = DistanceCalculator.getUnnormalizedDistance(c1, c2, cc);

        return this.uncd;
    }

    public double getNormalizedDistance() {
        if (ncd >= 0)
            return ncd;

        this.ncd = DistanceCalculator.getNormalizedDistance(c1, c2, cc);

        return this.ncd;
    }

    public long getC1() {
        return c1;
    }

    public long getC2() {
        return c2;
    }

    public long getCc() {
        return cc;
    }
}
