package similarity.model

import java.io.OutputStream
import similarity.compress.Compressor


class ComplexityCalculator(compressor : Compressor) {

  def complexity(text:  TextBlock) : Int = {
    val counter = new CountingOutputStream
    compressor.compress(text, counter)
    counter.counter
  }
}

/**
 * an output stream that only count how many bytes pass through it, without
 * storing anything
 */
class CountingOutputStream extends OutputStream {

  var counter = 0

  override def write(b: Int): Unit = {
      counter += 1;
  }

}