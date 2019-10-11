package inform.dist.nld.gist.combining;

import inform.dist.nld.gist.Gist;
import inform.dist.nld.gist.StringListGist;

import java.util.ArrayList;
import java.util.List;

public class ConcatenateForStringListGistPolicy extends GistCombiningPolicy {
    @Override
    public Gist combine(Gist g1, Gist g2) {

        StringListGist gist2 = (StringListGist) g2;
        StringListGist gist1 = (StringListGist) g1;

        List<String> combination = new ArrayList<String>((int) (gist1.size() + gist2.size()));
		combination.addAll(gist1.getStringList());
		combination.addAll(gist2.getStringList());

		return new StringListGist(combination);
    }
}
