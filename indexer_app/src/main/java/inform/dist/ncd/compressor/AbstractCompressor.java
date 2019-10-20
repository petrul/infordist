package inform.dist.ncd.compressor;

import inform.dist.ncd.gist.Gist;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractCompressor implements Compressor {

    static Map<Compressor.CmprType, Compressor> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put(CmprType.BZIP2, new Bzip2Compressor());
        REGISTRY.put(CmprType.GZIP, new GzipCompressor());
        REGISTRY.put(CmprType.PPM, new PPMCompressor());
        REGISTRY.put(CmprType.NONE, new NoCompressor());
    }

    public byte[] compress(Gist s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.compress(s, baos);
        return baos.toByteArray();
    }

    @Override
    public void compress(Gist s, OutputStream outputStream) {
        try {
            StopWatch watch = new StopWatch(); watch.start();

            OutputStream bzos = this.specificStream(outputStream);

            s.writeTo(bzos);

            bzos.flush();
            bzos.close();

            outputStream.flush();

            watch.stop();

            if (LOG.isDebugEnabled())
                LOG.debug("done compressing gist, took " + watch);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public long getComplexity(Gist s) {
        try {
            StopWatch watch = new StopWatch(); watch.start();
            CountingOutputStream counterStream = new CountingOutputStream();
            OutputStream bzos = this.specificStream(counterStream);
            s.writeTo(bzos);
            bzos.flush();
            bzos.close();

            long result = counterStream.getCounter();
            if (LOG.isDebugEnabled()) LOG.debug("done compressing gist, took " + watch);

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static Logger LOG = Logger.getLogger(AbstractCompressor.class);

}
