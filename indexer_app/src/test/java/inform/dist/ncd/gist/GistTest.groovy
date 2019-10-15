package inform.dist.ncd.gist

import org.junit.Test

class GistTest {

    @Test
    void gistSpec() {
        def url1 = this.class.classLoader.getResource("bzipped_files_for_ncd/emmi.bz2");
        def url2 = this.class.classLoader.getResource("bzipped_files_for_ncd/lb.bz2");
        println url1
        def file_length1 = new File(url1.getFile()).length()

        def g1 = new FileGist(url1)
        def complexity1 =  g1.computeComplexity(g1.initialCompressor)

        assert (complexity1 == file_length1)

        def g2 = new FileGist(url2)

        assert (new File(url2.file).length() == g2.computeComplexity(g2.initialCompressor))


    }

    @Test
    void testCombine() {
        def s1 = """
distinct win most _ award emmi histori 
most emmi award _ histori win nomin 
deschanel paul schrader _ award emmi academi 
schrader emmi award _ academi award oscar 
award academi award _ award guild award 
juli aol earn _ award emmi nomin 
earn emmi award _ nomin aol origin 
two news documentari _ nomin one maker 
historian educ ndash _ noether german american 
also win primetim _ award outstand comedi 
lost minut primetim _ award individu perform
"""



        def g1 = new StringGist(s1)
        println "======"
        println g1.nrLines()
        println "======"
        print g1.toString()
        println "======"

        println 10 / 30
    }
}
