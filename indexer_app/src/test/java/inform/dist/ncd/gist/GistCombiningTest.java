package inform.dist.ncd.gist;

import inform.dist.ncd.gist.combining.GistCombiningPolicy;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GistCombiningTest {


    private StringGist newRandomGist(int nrLines) {

        Random rnd = new Random();

        List<String> lines = new ArrayList<>(nrLines);

        for (int i = 0; i < nrLines; i++) {
            final int nrWords = 5;
            List<String> words = new ArrayList<>(nrWords);

            for (int j = 0; j < nrWords; j++) {
                int wordsize = rnd.nextInt(10) + 3;
                words.add(RandomStringUtils.randomAlphabetic(wordsize));
            }

            String line = String.join(" ", words);
            lines.add(line);
        }

        return new StringGist(String.join("\n", lines));

    }

    @Test
    public void testInterlaceEven() {

        StringGist gist1 = newRandomGist(10);
        StringGist gist2 = newRandomGist(3);

        List<String> list1 = gist1.getStringList();
        List<String> list2 = gist2.getStringList();

        System.out.println(gist1.toString());
        System.out.println(gist1.getStringList().size());
        System.out.println("****************");
        System.out.println(gist2.toString());
        System.out.println(gist2.getStringList().size());
        System.out.println("****************");


        Gist result = gist1.combine(gist2, GistCombiningPolicy.Policy.INTERLACE_EVEN);
        System.out.println(result.toString());

        assert result instanceof StringGist;
        StringGist res = (StringGist) result;

        assert res.nrLines() == gist1.nrLines() + gist2.nrLines();

        assert res.getStringList().size() == gist1.getStringList().size() + gist2.getStringList().size();
        assert res.getStringList().get(0).equals(list1.get(0)); // from the first

        List<String> res_sl = res.getStringList();
        for (String s : list1) {
            assert res_sl.contains(s);
        }
        for(String s: list2) {
            assert res_sl.contains(s);
        }

        assert res.nrLines() == gist1.nrLines() + gist2.nrLines();

    }


    @Test
    public void testConcatenateCombination() {
        StringGist g1 = this.newRandomGist(100);
        StringGist g2 = this.newRandomGist(200);

        assert g1.nrLines() == 100;
        assert g2.nrLines() == 200;
        StringGist res = g1.combine(g2, GistCombiningPolicy.Policy.CONCATENATE);

        Assert.assertEquals(100 + 200, res.nrLines());
    }
}