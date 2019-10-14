package inform.dist.nld.compressor;

import com.sun.org.apache.regexp.internal.RE;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Compressors  {
    public static Map<String, Compressor> REGISTRY = new HashMap<>();

    static {
        REGISTRY.put("bz2", new Bzip2Compressor());
        REGISTRY.put("gz", new GzipCompressor());
        REGISTRY.put("ppm", new PPMCompressor());
        REGISTRY.put("", new NoCompressor());
    }

    Set<String> getExtensions() {
        return REGISTRY.keySet();
    }
}
