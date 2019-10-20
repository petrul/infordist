package inform.dist.ncd.gist;

import inform.dist.ncd.compressor.Compressor;
import inform.dist.ncd.compressor.CountingOutputStream;
import inform.dist.ncd.gist.combining.GistCombiningPolicy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractGist implements Gist {

    @Override
    public void combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy, OutputStream outputStream) {
        GistCombiningPolicy policy = GistCombiningPolicy.REGISTRY.get(combiningPolicy);
        if (policy == null)
           throw new IllegalArgumentException("missing policy for combination of " + this.getClass() + " for " + combiningPolicy.toString());
        policy.combine(this,anotherGist, outputStream);
    }

    public void combine(Gist anotherGist, OutputStream outputStream) {
        this.combine(anotherGist, GistCombiningPolicy.Policy.INTERLACE_EVEN, outputStream);
    }

    /**
     * version of {@link AbstractGist#combine(Gist)} that works in-memory, so generates a StringGist
     * @param anotherGist
     * @return
     */
    public StringGist combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.combine(anotherGist, combiningPolicy, baos);
        String s = baos.toString();
        return new StringGist(s);
    }

    public StringGist combine(Gist anotherGist) {
        return this.combine(anotherGist, GistCombiningPolicy.Policy.INTERLACE_EVEN);
    }

    @Override
    public long computeComplexity(Compressor compressor) {
        return compressor.getComplexity(this);
    }

    public long computeCombinedComplexity(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy, Compressor compressor) {
        CountingOutputStream counter_os = new CountingOutputStream();
        OutputStream compressedOutputStream = compressor.specificStream(counter_os);
        this.combine(anotherGist, combiningPolicy, compressedOutputStream);
        try {
            compressedOutputStream.flush();
            compressedOutputStream.close();
            counter_os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return counter_os.getCounter();
    }
}
