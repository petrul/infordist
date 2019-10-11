package inform.dist.nld.gist.combining;

import inform.dist.nld.gist.Gist;
import inform.dist.nld.gist.StringListGist;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import java.util.*;

public class GistCombiningTest {


    private StringListGist newRandomGist(int nrLines) {

        Random rnd = new Random();

        List<String> lines = new ArrayList<>(nrLines);

        for (int i = 0; i< nrLines; i++) {
            final int nrWords = 5;
            List<String> words = new ArrayList<>(nrWords);

            for (int j = 0; j < nrWords; j++) {
                int wordsize = rnd.nextInt(10) + 3;
                words.add(RandomStringUtils.randomAlphabetic(wordsize));
            }

            String line = String.join(" ", words);
            lines.add(line);
        }

        return new StringListGist(lines);

    }

    @Test
    public void testInterlaceEven() {

        Gist gist1 = newRandomGist(10);
        Gist gist2 = newRandomGist(3);

        List<String> list1 = ((StringListGist) gist1).getStringList();
        List<String> list2 = ((StringListGist) gist2).getStringList();

        System.out.println(gist1.toString());
        System.out.println("****************");
        System.out.println(gist2.toString());
        System.out.println("****************");



        Gist result = gist1.combine(gist2, GistCombiningPolicy.Policy.INTERLACE_EVEN);
        System.out.println(result.toString());

        assert result instanceof StringListGist;
        StringListGist slg = (StringListGist) result;

        assert slg.size() == gist1.size() + gist2.size();

        assert slg.getStringList().size() == ((StringListGist) gist1).getStringList().size() + ((StringListGist) gist2).getStringList().size();
        assert slg.getStringList().get(0).equals(list1.get(0)); // three from the first
        assert slg.getStringList().get(1).equals(list1.get(1));
        assert slg.getStringList().get(2).equals(list1.get(2));
        assert slg.getStringList().get(3).equals(list2.get(0)); // one from the second
        assert slg.getStringList().get(4).equals(list1.get(3)); // first again...
    }


    @Test
    public void testConcatenateCombination() {
        StringListGist g1 = this.newRandomGist(100);
        StringListGist g2 = this.newRandomGist(200);

        StringListGist res = (StringListGist) g1.combine(g2, GistCombiningPolicy.Policy.CONCATENATE);

        assert res.size() == 100 + 200;
    }
}