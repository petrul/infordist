package similarity.model

import scala.math._

/**
 * calculates information distance, see work by Vitanyi & co
 */
object DistanceCalculator {

  /**
   * @param c1 C(x)
   * @param c2 C(y)
   * @param cc combined complexity, C(x,y)
   */
  def getNormalizedDistance(c1: Int, c2: Int, cc: Int): Double = {
    val cmax = max(c1, c2)
    getDistance(c1, c2, cc).asInstanceOf[Double] / cmax
  }

  /**
   * unnormalized distance
   */
  def getDistance(c1: Int, c2: Int, cc: Int): Int = {
    val cmin = min(c1, c2)
    cc - cmin
  }
}
