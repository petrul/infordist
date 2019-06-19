package ncd

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.RandomStringUtils
import org.apache.commons.lang.math.RandomUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

// a few specs on how the ncd-similarity package could be used.
class SpecTest {

    File tmpdir;

    @Before
    void before() {
        this.tmpdir = new File(System.getProperty("java.io.tmpdir"), RandomStringUtils.randomAlphabetic(10))
        this.tmpdir.mkdirs();
    }

    @After
    void after() {
        FileUtils.deleteDirectory(this.tmpdir)
    }

    @Test
    void testExisting() {
//        print(this.tmpdir)

        File bz2gists = new File("/Users/petru/ncd-wikipedia-lucene/gists.bz2")
        println bz2gists.list()

        // TODO here: just take one term and compute its nearest neighbours using existing code
    }

    @Test
    void testSpec1() {
        println 5
    }

}


