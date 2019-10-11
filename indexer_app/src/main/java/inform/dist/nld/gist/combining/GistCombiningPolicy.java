package inform.dist.nld.gist.combining;

import inform.dist.nld.gist.Gist;

import java.util.HashMap;
import java.util.Map;

/**
 * strategy used by @Gist#combine
 *
 * INTERLACE_EVEN is probably the way to go.
 */
public abstract class GistCombiningPolicy {

    public enum Policy  {
        CONCATENATE, // two gists are concatenated. for big gists, compression together might not work appropriately since most
                        // compression algorithms work on blocks so block1 might be gist1 and block2 might be gist2.
        INTERLACE_TOP,  // one line from a gist, another from the other one. for gists of different sizes,
                        // when the smaller gist ends, the rest of lines of
                        // the larger gist continue unmixed
        INTERLACE_EVEN;
                        // interlaced but evenly. if we have a gist G1 of 200 lines and G2 of 100 lines,
                        // after two lines of G1 will be interlaced one line of G2

    }

    public static Map<Policy, GistCombiningPolicy> REGISTRY = new HashMap<>();
    static  {
        REGISTRY.put(Policy.INTERLACE_EVEN, new InterlaceEvenForStringListGistPolicy());
        REGISTRY.put(Policy.CONCATENATE, new ConcatenateForStringListGistPolicy());
    }

    public abstract Gist combine(Gist g1, Gist g2);
}
