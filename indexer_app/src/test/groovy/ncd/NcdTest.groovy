package ncd

import inform.dist.ncd.gist.FileGist
import inform.dist.ncd.gist.Gist
import org.apache.commons.lang.time.StopWatch
import org.junit.Test

class NcdTest {

    @Test
    void test_ncd() {
        // this takes forever, there's a bug somewhere
        def url1 = this.class.classLoader.getResource("bzipped_files_for_ncd/emmi.bz2")

        Gist g1 = new FileGist(url1);
        def compressor = g1.initialCompressor

        StopWatch watch = new StopWatch(); watch.start()
        println g1.sizeInBytes // this should be read from the file
        def g1_c = g1.computeComplexity(compressor); // this should be computed by recompressing the decompressed
        println "g1_complexity = $g1_c"
        assert g1.sizeInBytes == g1_c
        watch.stop();
        println "computed complexity of g1, took ${watch.toString()}"

        watch.reset(); watch.start()
        Gist g2 = new FileGist(this.class.classLoader.getResource("bzipped_files_for_ncd/lb.bz2"))
        assert g2.initialCompressor == g1.initialCompressor
        println "**" * 20

        def g2_c =  g2.computeComplexity(compressor)
        println "g2_c = $g2_c"
        println g2.sizeInBytes
        watch.stop();
        println "computed complexity of g2, took ${watch.toString()}"

        println "=" * 20

        watch.reset(); watch.start()

        def res = g1.combine(g2)
        println res.computeComplexity(compressor)

        watch.stop();
        println "computed combined complexity of g1,g2, took ${watch.toString()}"
        println "=" * 60


    }
}
