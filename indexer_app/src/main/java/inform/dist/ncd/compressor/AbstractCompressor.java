package inform.dist.ncd.compressor;

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
}
