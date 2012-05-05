package similarity.scripts

import java.io.File
import similarity.model.{DistanceCalculator, TextBlock, ComplexityCalculator}
import similarity.compress.{GzipCompressor, PpmCompressor, Bzip2Compressor}

object CalculateNearestDreams {

  def main(args: Array[String]) {
    val viseDir = new File("/home/petru/work/vise/data/vise_texte")
    val allDreams = viseDir.listFiles.toList

    val fileToTake = new File(viseDir, args(0))

    val compressor: Bzip2Compressor = new Bzip2Compressor
//    val compressor = new PpmCompressor
//    val compressor = new GzipCompressor
    val ccalc = new ComplexityCalculator(compressor)

    val mainText = new TextBlock(fileToTake)
    println("length for " + fileToTake + " is " + mainText.length)
    val main_c = ccalc.complexity(mainText)
    Console println main_c


    // a wrapper to put several thing together
    class TextInformation(text: TextBlock, complexity: Int, combinedComplexity: Int,
                          distance: Int, normalizedDistance: Double) {
      def normdist = this.normalizedDistance
      def dist = distance
      override def toString = text.toString + "(" + normalizedDistance + ")"
    }

    val allinfo = allDreams.map { (f : File) =>
      val thattext = new TextBlock(f)
      val that_c = ccalc.complexity(thattext)
      val cc = ccalc.complexity(mainText + thattext)
      val dist = DistanceCalculator.getDistance(main_c, that_c, cc)
      val normdist = DistanceCalculator.getNormalizedDistance(main_c, that_c, cc)

      new TextInformation(thattext, that_c, cc, dist, normdist)
    }

    val sorted_list = allinfo.sortWith { (e1, e2) =>
      e1.normdist < e2.normdist
    }

    sorted_list.foreach {println}
  }
}
