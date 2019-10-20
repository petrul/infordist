package inform.dist

import inform.dist.ncd.compressor.Bzip2Compressor
import inform.dist.ncd.gist.combining.GistCombiningPolicy
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.Test

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * make sure compression works and combining works and combined compression works.
 */
class FileNCDCalculatorTest {


    def compute_length_of_bzipped2_string(String s) {
        compress_bzip2(s).length()
    }

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
    public void ncd_Ottoman_Turk() {
        def ottoman = this.class.classLoader.getResource("bzipped_files_for_ncd/450k/ottoman.bz2")
        def turk = this.class.classLoader.getResource("bzipped_files_for_ncd/450k/turk.bz2")

        String ottoman_uncompressed = uncompress_bzip2(new File(ottoman.getFile()))
        byte[] ottoman_uncompressed_bytes = ottoman_uncompressed.getBytes(StandardCharsets.UTF_8)
        assert ottoman_uncompressed_bytes.length == 450000

        def bzip2 = new Bzip2Compressor()
        def calc = new FileNCDCalculator(ottoman, turk, bzip2, GistCombiningPolicy.Policy.CONCATENATE)
//        def calc = new FileNCDCalculator(ottoman, turk, bzip2, GistCombiningPolicy.Policy.INTERLACE_EVEN)
        calc.compute()

        new File("/Users/petru/lalala-ottoman.bz2").withOutputStream {
            it.write(compress_bzip2(uncompress_bzip2(new File(ottoman.getFile()))))
            it.flush()
        }

        println ("C(ottoman) = ${calc.c1}")
        assert calc.c1 == new File(ottoman.getFile()).length() // 109871 vs 109878 slight differences of size
        assert calc.c1 == compress_bzip2(uncompress_bzip2(new File(ottoman.getFile()))).length
        assert calc.c1 == new File(ottoman.getFile()).bytes.length

        println calc.c2
        assert (calc.c2 == new File(turk.getFile()).getBytes(StandardCharsets.UTF_8).length)

        println calc.cc
        assert (calc.cc < calc.c1 + calc.c2)

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