package inform.dist

import inform.dist.ncd.compressor.Bzip2Compressor
import inform.dist.ncd.gist.FileGist
import inform.dist.ncd.gist.combining.GistCombiningPolicy
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.Test

import java.nio.charset.StandardCharsets

/**
 * make sure compression works and combining works and combined compression works.
 */
class FileNCDCalculatorTest {

    byte[] compress_bzip2(String s) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        BZip2CompressorOutputStream bz2 = new BZip2CompressorOutputStream(baos)
        bz2.withWriter { wr -> wr.write(s)}
        bz2.close()
        baos.close()
        baos.toByteArray()
    }

    String uncompress_bzip2(File bz2file) {
        def fileInputStream = new FileInputStream(bz2file)
        BZip2CompressorInputStream bzin = new BZip2CompressorInputStream(fileInputStream)
        String res = bzin.text
        bzin.close()
        fileInputStream.close()
        res
    }

    @Test
    public void ncd_Ottoman_Turk_Conc() {
        this.ncd_Ottoman_TurkForPolicy(GistCombiningPolicy.Policy.CONCATENATE)
    }

    @Test
    public void ncd_Ottoman_Turk_Interlace_Even() {
        this.ncd_Ottoman_TurkForPolicy(GistCombiningPolicy.Policy.INTERLACE_EVEN)
    }


    protected void ncd_Ottoman_TurkForPolicy(final GistCombiningPolicy.Policy policy) {
        URL ottoman = this.class.classLoader.getResource("bzipped_files_for_ncd/450k/ottoman.bz2")
        def turk = this.class.classLoader.getResource("bzipped_files_for_ncd/450k/turk.bz2")

        String ottoman_uncompressed = uncompress_bzip2(new File(ottoman.getFile()))
        byte[] ottoman_uncompressed_bytes = ottoman_uncompressed.getBytes(StandardCharsets.UTF_8)
        assert ottoman_uncompressed_bytes.length == 450000

        def bzip2 = new Bzip2Compressor()
        def calc = new FileNCDCalculator(ottoman, turk, bzip2, policy)
//        def calc = new FileNCDCalculator(ottoman, turk, bzip2, GistCombiningPolicy.Policy.INTERLACE_EVEN)
        calc.compute()


        println ("C(ottoman) = ${calc.c1}")
        assert calc.c1 == compress_bzip2(uncompress_bzip2(new File(ottoman.getFile()))).length // verify how the intricate OOP engineering is actually done with the bit of compression code here
        //  assert calc.c1 == new File(ottoman.getFile()).length() // 109871 vs 109878 slight differences of compression between apache commons compress bz2 and command line gnu bzip2 tools
        //  assert calc.c1 == new File(ottoman.getFile()).bytes.length // same note

        println ("C(turk) = ${calc.c2}")
        println calc.c2
//        assert (calc.c2 == new File(turk.getFile()).getBytes().length)  // same note

        println ("CC(ottoman,turk) = ${calc.cc}")
        println calc.cc
        assert (calc.cc < calc.c1 + calc.c2)


        FileGist ottomanGist = new FileGist(ottoman)
        FileGist turkGist = new FileGist(turk)

        // this is how ncd_for_dir does. FileNCDCalculator uses a StringGist (memory) for combination, wherease the AbstractGist#computeCombinedComplexity uses a CountingStream in the aether
        // very important: two different albeit simple bits of code to compute combined complexity
        assert calc.cc == ottomanGist.computeCombinedComplexity(turkGist, policy, bzip2)

        def normalizedDistance = calc.getNormalizedDistance()
        println normalizedDistance
        assert normalizedDistance < 1.0

    }


    @Test
    void ncd_Ottoman_Itself() {
        def ottoman = this.class.classLoader.getResource("bzipped_files_for_ncd/ottoman.bz2")
        def ottoman2 = this.class.classLoader.getResource("bzipped_files_for_ncd/ottoman.bz2")

        def bzip2 = new Bzip2Compressor()
        def calc = new FileNCDCalculator(ottoman, ottoman2, bzip2, GistCombiningPolicy.Policy.CONCATENATE)
        calc.compute()
        println calc.c1
        assert (calc.c1 == new File(ottoman.getFile()).length())
        println calc.c2
        assert (calc.c2 == new File(ottoman2.getFile()).length())
        println calc.cc
        assert (calc.cc < calc.c1 + calc.c2)

        println calc.getNormalizedDistance()
    }

}