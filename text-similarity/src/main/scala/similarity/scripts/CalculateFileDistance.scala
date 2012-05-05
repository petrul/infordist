package similarity.scripts

import java.io.File
import similarity.model.{DistanceCalculator, TextBlock, ComplexityCalculator}
import similarity.compress.{PpmCompressor, Bzip2Compressor}

object CalculateFileDistance {

  def main(args: Array[String]) {
    val viseDir = new File("/home/petru/work/vise/data/vise_texte")
    val file1 = new File(viseDir, args(0))
    val file2 = new File(viseDir, args(1))

//    val compressor = new Bzip2Compressor
    val compressor = new PpmCompressor
    val ccalc = new ComplexityCalculator(compressor)
    val text1 = new TextBlock(file1)
    println("length for " + file1 + " is " + text1.length)
    val c1 = ccalc.complexity(text1)
    Console println c1

    val text2 = new TextBlock(file2)
    println("length for " + file2 + " is " + text2.length)
    val c2 = ccalc.complexity(text2)
    Console println c2

    val cc = ccalc.complexity(text1 + text2)
    Console println cc

    val id = DistanceCalculator.getDistance(c1, c2, cc)
    val nid = DistanceCalculator.getNormalizedDistance(c1, c2, cc)

    println("infordist : " + id + ", nid " + nid)
  }
}