package similarity.cli

import java.io.File
import store.termmatrix.TermMatrixRW
import similarity.model.{ComplexityCalculator, TextBlock}
import similarity.compress.Bzip2Compressor
import io.Codec


/**
 * run this CLI on a directory of files and it will compute a matrix with complexities
 * and combined complexities
 */
object PairwiseSimilarityDistanceForDirectoryOfFilesCli {

  def main(args: Array[String]) {
    val dir = args(0)
    val listOfFiles = new File(dir).listFiles().toList.slice(0,20)
    def names(files: List[File]) = files.map(_.getName)

    val store = new TermMatrixRW(names(listOfFiles).toArray, new File("/tmp/mymatrix"), 100, "gutenberg texts")
    try { store.setStringVariable("directory", dir) } catch { case _ => () }

    val compressor: Bzip2Compressor = new Bzip2Compressor
//    val compressor = new PpmCompressor
//    val compressor = new GzipCompressor
    val ccalc = new ComplexityCalculator(compressor)

    def newBlock(f : File) = new TextBlock(f, Codec.ISO8859.name)

    println("calculating simple complexities...")
    for(val i <- 0 to listOfFiles.length - 1) {
      val file = listOfFiles(i)
      val c = ccalc.complexity(newBlock(file))
      store.setComplexity(file.getName, c)
    }

    println("calculating combined complexities...")
    for(val i <- 0 to listOfFiles.length - 1) {
      println("cc for " + listOfFiles(i))
      val mainBlock = newBlock(listOfFiles(i))
      for (val j <- i to listOfFiles.length - 1) {
        val theTwoBlocks = mainBlock + newBlock(listOfFiles(j))
        val cc = ccalc.complexity(theTwoBlocks)
        store.setCombinedComplexity(listOfFiles(i).getName, listOfFiles(j).getName, cc)
      }
    }

    store.close;
  }



//  // a wrapper to put several thing together
//  class TextInformation(text: TextBlock, complexity: Int, combinedComplexity: Int,
//                        distance: Int, normalizedDistance: Double) {
//    def normdist = this.normalizedDistance
//    def dist = distance
//    override def toString = text.toString + "(" + normalizedDistance + ")"
//  }
}
