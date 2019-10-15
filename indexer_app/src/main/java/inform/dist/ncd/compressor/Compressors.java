package inform.dist.ncd.compressor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Compressors {
    public static Map<String, Compressor> REGISTRY = new HashMap<>();
    public static Compressor NO_COMPRESSOR = new NoCompressor();

    static {
        REGISTRY.put("bz2", new Bzip2Compressor());
        REGISTRY.put("gz", new GzipCompressor());
        REGISTRY.put("ppm", new PPMCompressor());
        REGISTRY.put("", NO_COMPRESSOR);
    }

    Set<String> getExtensions() {
        return REGISTRY.keySet();
    }


    public static Compressor getCompressorForFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition == -1)
            return NO_COMPRESSOR;
        String extension = filename.substring(dotPosition + 1);
        final Compressor compressor = REGISTRY.get(extension);
        if (compressor == null)
            throw new IllegalArgumentException(String.format("don't know what compressor corresponds to extension" +
                    " %s for filename %s", extension, filename));
        return compressor;
    }
}
