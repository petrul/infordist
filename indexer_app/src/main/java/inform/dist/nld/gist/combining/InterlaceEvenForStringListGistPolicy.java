package inform.dist.nld.gist.combining;

import inform.dist.nld.gist.Gist;
import inform.dist.nld.gist.StringListGist;

import java.util.ArrayList;
import java.util.List;

/**
 * this is the description of combining in INTERLACE_EVEN mode for StringListGists
 */
public class InterlaceEvenForStringListGistPolicy extends GistCombiningPolicy {
    @Override
    public Gist combine(Gist g1, Gist g2) {

        StringListGist gist1 = (StringListGist) g1;
        StringListGist gist2 = (StringListGist) g2;

        List<String> resulting = new ArrayList<String>((int)(gist1.size() + gist2.size()));

        if (g1.size() < g2.size()) {
            gist1 = (StringListGist) g2;
            gist2 = (StringListGist) g1;
        }

        // now we're sure gist1 > gist2
        int ratio = (int) (g1.size() / g2.size());

        // for every 'ratio' lines of g1, we'll have a line of g2

        List<String> gist1_sl = gist1.getStringList();
        List<String> gist2_sl = gist2.getStringList();

        int gist1_counter = 0;
        int gist2_counter = 0;

        while (gist1_counter < gist1.size() || gist2_counter < gist2.size()) {

            // r from gist1
            for (int r = 0; r < ratio && gist1_counter < gist1.size(); r++) {
                String crt = gist1_sl.get(gist1_counter++);
                resulting.add(crt);
            }

            // then 1 from gist2
            if (gist2_counter < gist2.size())
                resulting.add(gist2_sl.get(gist2_counter++));

        }


        return new StringListGist(resulting);

    }
}