package matrix.store

import org.apache.commons.lang.RandomStringUtils
import org.junit.After
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

class ResizableTermMatrixTest {

    File f = new File("/tmp/" + RandomStringUtils.randomAlphabetic(10))

    @Before
    void before() {
        f.mkdirs()
    }

    @After
    void after() {
        f.deleteDir()
    }

    @Test
    void setCombinedComplexity() {
        def tm = new ResizableTermMatrix(10, this.f)
//        tm.
    }
}