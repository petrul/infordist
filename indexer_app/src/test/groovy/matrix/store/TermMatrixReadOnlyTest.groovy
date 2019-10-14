package matrix.store

import org.apache.commons.io.FileUtils
import org.apache.commons.lang.RandomStringUtils
import org.junit.After;
import org.junit.Before;
import org.junit.Test

class TermMatrixReadOnlyTest {


    File tempDir
    File file

    String[] terms = ["remus", "romulus", "singaporus", "papagalus"]

    @Before
    void setUp() throws Exception {

        def rnd = new Random()
        this.tempDir = new File(System.getProperty("java.io.tmpdir"), this.getClass().getCanonicalName());
        this.tempDir.mkdirs()
        this.file = new File(this.tempDir, RandomStringUtils.randomAlphabetic(10) + ".termmatrix");


        Map<String,Long> variables = [ "a": 1L, "b" : 2L]

        def matrix = new TermMatrixRW(terms, variables, file, "original comment")


        terms.each {
            assert matrix.getComplexity(it) == -1
            matrix.setComplexity(it, rnd.nextInt())
        }

        terms.each { i ->
            terms.each { j ->
                assert matrix.getCombinedComplexity(i, j) == -1
                matrix.setCombinedComplexity(i, j, Math.abs(rnd.nextInt()))
            }
        }
        matrix.close()
    }

    @After
    void tearDown() {
        FileUtils.deleteDirectory(this.tempDir)
    }

    @Test
    void tests() {

        TermMatrixReadOnly tm = new TermMatrixReadOnly(this.file)

        def remus = "remus"
        def romulus = "romulus"

        assert tm.getComplexity(remus) != -1
        assert tm.getCombinedComplexity(remus, romulus) != -1

        println "romulus = ${tm.getCombinedComplexity(remus, romulus)}"
        assert (tm.getCombinedComplexityRow("remus").length == 4)
        assert (tm.getCombinedComplexityRow("remus").length == 4) // the second time I call the method, java.nio.BufferUnderflowException
        tm.getCombinedComplexityRow("remus").each {
            assert it != -1
        }

        assert tm.getTermIndex("remus") == 0
        assert tm.getTermIndex("romulus") == 1

        assert tm.getTerms() == this.terms

        this.terms.eachWithIndex { elem, idx ->  assert tm.getTerm(idx) == elem }

        try {
            tm.setCombinedComplexity("remus", "romulus", 14)
            fail("should not be allowed")
        } catch (RuntimeException e) {
            // good
        }

        try {
            tm.setComplexity("remus", 14)
            fail("should not be allowed")
        } catch (RuntimeException e) {
            // good
        }

        assert tm.getComplexity("remus") != -1
        assert tm.getVariable("a") != -1


    }


}