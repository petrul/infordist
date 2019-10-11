package inform.dist.nld.gist;

import inform.dist.nld.gist.combining.GistCombiningPolicy;

public abstract class AbstractGist implements Gist {

    @Override
    public Gist combine(Gist anotherGist, GistCombiningPolicy.Policy combiningPolicy) {
        GistCombiningPolicy policy = GistCombiningPolicy.REGISTRY.get(combiningPolicy);
        if (policy == null)
            throw new IllegalArgumentException("missing policy for combination of " + this.getClass() + " for " + combiningPolicy.toString());
        return policy.combine(this,anotherGist);
    }

    public Gist combine(Gist anotherGist) {
        return this.combine(anotherGist, GistCombiningPolicy.Policy.INTERLACE_EVEN);
    }
}
