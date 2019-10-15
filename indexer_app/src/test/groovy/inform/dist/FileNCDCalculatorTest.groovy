package inform.dist

import inform.dist.ncd.compressor.Bzip2Compressor;
import org.junit.Test;

import static org.junit.Assert.*;

class FileNCDCalculatorTest {

    @Test
    public void ncd_Ottoman_Turk() {
        def ottoman = this.class.classLoader.getResource("bzipped_files_for_ncd/ottoman.bz2")
        def turk = this.class.classLoader.getResource("bzipped_files_for_ncd/turk.bz2")

        def bzip2 = new Bzip2Compressor()
        def calc = new FileNCDCalculator(ottoman, turk, bzip2)
        calc.compute()
        println calc.c1
        assert (calc.c1 == new File(ottoman.getFile()).length())
        println calc.c2
        assert (calc.c2 == new File(turk.getFile()).length())
        println calc.cc
        assert (calc.cc < calc.c1 + calc.c2)
        println calc.getNormalizedDistance()

    }


    @Test
    void ncd_Ottoman_Itself() {
        def ottoman = this.class.classLoader.getResource("bzipped_files_for_ncd/ottoman.bz2")
        def ottoman2 = this.class.classLoader.getResource("bzipped_files_for_ncd/ottoman.bz2")

        def bzip2 = new Bzip2Compressor()
        def calc = new FileNCDCalculator(ottoman, ottoman2, bzip2)
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